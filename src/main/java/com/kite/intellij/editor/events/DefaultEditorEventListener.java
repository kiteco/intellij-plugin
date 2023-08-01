package com.kite.intellij.editor.events;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intellij.ide.FrameStateListener;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.FocusChangeListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.util.Alarm;
import com.kite.intellij.KiteConstants;
import com.kite.intellij.KiteProjectLifecycleService;
import com.kite.intellij.backend.KiteServerSettingsService;
import com.kite.intellij.backend.model.EventType;
import com.kite.intellij.backend.model.TextSelection;
import com.kite.intellij.editor.util.FileEditorUtil;
import com.kite.intellij.lang.KiteLanguageSupport;
import com.kite.intellij.platform.KitePlatform;
import com.kite.intellij.platform.fs.CanonicalFilePath;
import com.kite.intellij.platform.fs.CanonicalFilePathFactory;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Map;
import java.util.Set;

/**
 * Listens to IntelliJ's events and executes the coresponding Kite events whenever necessary.
 * <p>
 * It will only react to events if the current platform is supported by Kite.
 *
 */
public class DefaultEditorEventListener implements EditorEventListener, ProjectActivity, FrameStateListener, Disposable {
    private Project myProject;
    private static final Logger LOG = Logger.getInstance("#kite.editorEvent");
    private static final Key<Boolean> KEY_FLUSH_ON_DOC_COMMIT = Key.create("kite.docFlush");

    protected final Alarm editAlarm;
    protected final Map<Document, Runnable> pendingEditorChangeRequests = Maps.newHashMap();
    private final KiteEventQueue eventQueue;
    private final int alarmDelayMillis;
    private final Set<CanonicalFilePath> currentlyModifiedDocuments = Sets.newConcurrentHashSet();
    private final CanonicalFilePathFactory filePathFactory;
    private FileEditorManager fileEditorManager;

    //lock to synchronize on when accessing the request event maps
    private final Object requestLock = new Object();

    protected DefaultEditorEventListener() {
        this.eventQueue = new AsyncKiteEventQueue();
        this.alarmDelayMillis = KiteConstants.ALARM_DELAY_MILLIS;
        this.filePathFactory = CanonicalFilePathFactory.getInstance();

        this.editAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, this);
    }

    @Override
    public KiteEventQueue getEventQueue() {
        return eventQueue;
    }

    public void awaitEvents() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("awaitEvents");
        }

        if (isEventDispatchThread()) {
            synchronized (requestLock) {
                //editAlarm.flush() can not be used because it doesn't wrap with an outer write session
                //and calls invokeLater for the runnables
                editAlarm.cancelAllRequests();

                //we need to iterate on a copy because the runnables remove the map entry on their own
                if (!pendingEditorChangeRequests.isEmpty()) {
                    for (Runnable runnable : Lists.newArrayList(pendingEditorChangeRequests.values())) {
                        runnable.run();
                    }

                    pendingEditorChangeRequests.clear();
                }
            }
        }
    }

    @Override
    public void dispose() {
        eventQueue.stop();
    }


    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        this.myProject = project;
        this.fileEditorManager = FileEditorManager.getInstance(myProject);

        if (!KitePlatform.isOsVersionSupported()) {
            return null;
        }

        Disposer.register(project.getService(KiteProjectLifecycleService.class), this);

        // This is strange. why could it be started twice?
        if (!eventQueue.isRunning())
            eventQueue.start();

        EditorFactory editorFactory = EditorFactory.getInstance();

        EditorEventMulticaster eventMulticaster = editorFactory.getEventMulticaster();

        eventMulticaster.addDocumentListener(new DocumentListener() {
            @Override
            public void beforeDocumentChange(@NotNull DocumentEvent e) {
                Document document = e.getDocument();

                VirtualFile file = FileDocumentManager.getInstance().getFile(document);
                if (file == null || !file.isInLocalFileSystem()) {
                    return;
                }

                if (!Boolean.TRUE.equals(KEY_FLUSH_ON_DOC_COMMIT.get(document))) {
                    KEY_FLUSH_ON_DOC_COMMIT.set(document, Boolean.TRUE);

                    //we need to flush pending events (for the current file) on document flush
                    //the code completion commits and then calls the completion provider
                    //we can't flush pending edit events in our completion provider because this will lead to a dead lock
                    // - EDT calls completion in a pooled thread
                    // - pooled thread calls completion -> completion calls flush -> flushed actions must be run on EDT
                    // - waiting on the EDT here will dead-lock our application
                    // we're not invoking PsiDocumentManagerBase.addRunOnCommit directly,
                    // because it changed to a non-static method in 2020.2
                    PsiDocumentManager.getInstance(myProject).performForCommittedDocument(document, () -> {
                        //the commit action is only run once, we need to re-register before the next change event
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("awaitEvents on commit");
                        }

                        try {
                            awaitEvents();
                        } finally {
                            KEY_FLUSH_ON_DOC_COMMIT.set(document, null);
                        }
                    });
                }

                //this call must not be added to the alarm ticker because Alarm on 171.x doesn't seem to guarantee
                //execution order and thus the beforeDocumentChange event might be called after the documentChange
                //which is added later to the alarm and this breaks our logic
                //beforeDocumentChange is rather cheap, so we call it directly
                DefaultEditorEventListener.this.beforeDocumentChange(e);
            }

            @Override
            public void documentChanged(@NotNull DocumentEvent e) {
                Document document = e.getDocument();
                VirtualFile file = FileDocumentManager.getInstance().getFile(document);
                if (file == null || !file.isInLocalFileSystem()) {
                    return;
                }

                if (LOG.isTraceEnabled()) {
                    LOG.trace("Edit event \"" + e.getNewFragment() + "\"");
                }

                addOverridingRequest(pendingEditorChangeRequests, document, editAlarm, () -> DefaultEditorEventListener.this.documentChanged(e));
            }
        }, this);

        eventMulticaster.addCaretListener(new CaretListener() {
            @Override
            public void caretPositionChanged(@NotNull CaretEvent e) {
                Document document = e.getEditor().getDocument();

                VirtualFile file = FileDocumentManager.getInstance().getFile(document);
                if (file == null || !file.isInLocalFileSystem()) {
                    return;
                }

                addOverridingRequest(pendingEditorChangeRequests, document, editAlarm, () -> DefaultEditorEventListener.this.caretPositionChanged(e));

            }
        }, this);

        // We don't subscribe with myProject.getMessageBus().connect(myProject).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER
        // because the the FILE_EDITOR_MANAGER event is only triggered when a new editor is created,
        // we listen for focus changes (e.g. by tab activation) below but attach to each new editor
        // we must handle editors restored at startup, though. Extra careful testing in the new IJ branches must be
        // done to make sure that restored files generate focus events
        editorFactory.addEditorFactoryListener(new EditorFactoryListener() {
            @Override
            public void editorCreated(@NotNull EditorFactoryEvent event) {
                Editor editor = event.getEditor();
                if (!(editor instanceof EditorEx)) {
                    return;
                }

                ((EditorEx) editor).addFocusListener(new FocusChangeListener() {
                    @Override
                    public void focusGained(@NotNull Editor editor) {
                        if (editor instanceof EditorEx) {
                            VirtualFile virtualFile = ((EditorEx) editor).getVirtualFile();
                            if (virtualFile != null) {
                                DefaultEditorEventListener.this.fileFocused(editor, virtualFile);
                            }
                        }
                    }

                    @Override
                    public void focusLost(@NotNull Editor editor) {
                    }
                }, DefaultEditorEventListener.this);
            }
        }, this);

        ApplicationManager.getApplication().getMessageBus().connect(this).subscribe(FrameStateListener.TOPIC, this);

        return null;
    }

    /**
     * Called when the IntelliJ window's frame is activated.
     * If there is a current editor open and if it has the focus then a focus event is send to kite.
     */
    @Override
    public void onFrameActivated(@NotNull IdeFrame frame) {
        Editor editor = fileEditorManager.getSelectedTextEditor();
        if (editor == null) {
            return;
        }

        CanonicalFilePath path = filePathFactory.createFor(editor, CanonicalFilePathFactory.Context.Event);
        if (path != null) {
            eventQueue.addEvent(KiteEventFactory.create(EventType.FOCUS, path, editor));
            if (LOG.isDebugEnabled()) {
                LOG.debug("Focus event after frame activation");
            }
        }
    }

    public void fileFocused(@Nonnull Editor editor, @Nonnull VirtualFile file) {
        if (editor.isDisposed() || editor.isOneLineMode() || editor.isViewer()) {
            return;
        }

        CanonicalFilePath filePath = filePathFactory.createFor(editor, CanonicalFilePathFactory.Context.Event);
        if (filePath == null) {
            return;
        }

        if (file.getLength() > KiteServerSettingsService.getInstance().getMaxFileSizeBytes()) {
            eventQueue.addEvent(KiteEventFactory.createSkipEvent(filePath));
        } else {
            eventQueue.addEvent(KiteEventFactory.create(EventType.FOCUS, filePath, editor));
        }
    }

    /**
     * Registers a new request which handles an editor event.
     *
     * @param requestMap The map where the new request is inserted and any previous event of the same kind is removed from
     * @param key
     * @param alarm
     * @param newRequest
     * @param <T>
     */
    protected <T> void addOverridingRequest(Map<T, Runnable> requestMap, T key, Alarm alarm, Runnable newRequest) {
        synchronized (requestLock) {
            Runnable oldRequest = requestMap.get(key);
            if (oldRequest != null) {
                requestMap.remove(key);
                alarm.cancelRequest(oldRequest);

                if (LOG.isTraceEnabled()) {
                    LOG.trace("Dropping request due to new editor action");
                }
            }
        }

        Runnable runnable = () -> {
            //this is not totally safe because (in theory) it might happen that there was another key added for key in the meantime
            synchronized (requestLock) {
                requestMap.remove(key);
            }

            newRequest.run();
        };

        synchronized (requestLock) {
            requestMap.put(key, runnable);
        }

        alarm.addRequest(runnable, alarmDelayMillis);
    }

    private void caretPositionChanged(CaretEvent e) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("caretPositionChanged");
        }

        Caret caret = e.getCaret();
        if (caret == null) {
            return;
        }

        Editor editor = e.getEditor();
        if (editor.isDisposed()) {
            return;
        }

        CanonicalFilePath filePath = filePathFactory.createFor(e.getEditor(), CanonicalFilePathFactory.Context.Event);
        if (filePath == null) {
            return;
        }

        if (e.getEditor().getDocument().getTextLength() > KiteServerSettingsService.getInstance().getMaxFileSizeBytes()) {
            //text length is != byte length but decoding is expensive
            if (LOG.isTraceEnabled()) {
                LOG.trace("Sending skip event instead of caret change for  " + filePath);
            }
            eventQueue.addEvent(KiteEventFactory.createSkipEvent(filePath));
            return;
        }

        TextSelection selection;
        if (caret.hasSelection()) {
            selection = TextSelection.create(caret.getSelectionStart(), caret.getSelectionEnd());
        } else {
            selection = TextSelection.create(caret.getOffset());
        }

        // http status listener updates only if the current file is supported by Kite,
        boolean supported = KiteLanguageSupport.isSupported(editor, KiteLanguageSupport.Feature.BasicSupport);
        eventQueue.addEvent(KiteEventFactory.create(EventType.SELECTION, filePath, FileEditorUtil.contentOf(editor), selection, editor.getDocument(), supported));
    }

    private void beforeDocumentChange(DocumentEvent event) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("beforeDocumentChanged");
        }

        Document document = event.getDocument();
        if (document instanceof DocumentEx && document.isInBulkUpdate()) {
            return;
        }

        CanonicalFilePath filePath = filePathFactory.createFor(document, CanonicalFilePathFactory.Context.Event);
        if (filePath != null && document.getTextLength() <= KiteServerSettingsService.getInstance().getMaxFileSizeBytes()) {
            //text length is != file size with non-ascii content. decoding into bytes is expensive, though.
            currentlyModifiedDocuments.add(filePath);
        }
    }

    private void documentChanged(DocumentEvent event) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("documentChanged");
        }

        Document document = event.getDocument();
        if (document instanceof DocumentEx && document.isInBulkUpdate()) {
            return;
        }

        CanonicalFilePath filePath = filePathFactory.createFor(document, CanonicalFilePathFactory.Context.Event);
        if (filePath == null) {
            //may happen in test cases or for documents which do not represent a file system element, e.g. a LightVirtualFile used to do code completions etc.
            return;
        }

        try {
            final Editor textEditor = fileEditorManager.getSelectedTextEditor();
            if (textEditor == null || !textEditor.getDocument().equals(document)) {
                //a document modification without an editor could be a refactoring
                //IntelliJ does not automatically save changes after a refactoring, so have to send events for these files, too
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Ignoring change outside of the current editor: " + filePath.asOSDelimitedPath());
                }
                return;
            }

            //null file paths might happen in test cases, for example, when the document is not yet attached to an actual VirtualFile/PsiFile.
            if (currentlyModifiedDocuments.contains(filePath)) {
                int editorOffset = textEditor.getCaretModel().getOffset();
                eventQueue.addEvent(KiteEventFactory.create(EventType.EDIT, filePath, FileEditorUtil.contentOf(document), TextSelection.create(editorOffset), document, true));
            }
        } finally {
            currentlyModifiedDocuments.remove(filePath);
        }
    }

    // taken from JetBrain's Alarm as it was removed from 183.x
    private static boolean isEventDispatchThread() {
        Application app = ApplicationManager.getApplication();
        return app != null && app.isDispatchThread() || EventQueue.isDispatchThread();
    }
}

package com.kite.intellij.action.signatureInfo;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.HintHint;
import com.intellij.ui.LightweightHint;
import com.intellij.ui.ScreenUtil;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.Alarm;
import com.intellij.util.ui.JBDimension;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import com.kite.intellij.KiteConstants;
import com.kite.intellij.action.FallbackToOriginalException;
import com.kite.intellij.backend.http.HttpStatusException;
import com.kite.intellij.backend.http.KiteHttpException;
import com.kite.intellij.backend.model.Call;
import com.kite.intellij.backend.model.Calls;
import com.kite.intellij.backend.model.Detail;
import com.kite.intellij.backend.model.PythonFunctionDetails;
import com.kite.intellij.backend.model.PythonTypeDetails;
import com.kite.intellij.editor.util.FileEditorUtil;
import com.kite.intellij.lang.documentation.KiteDocPsiLocator;
import com.kite.intellij.lang.documentation.KiteDocumentationRenderer;
import com.kite.intellij.lang.documentation.KiteDocumentationRendererService;
import com.kite.intellij.lang.documentation.LinksHandlers;
import com.kite.intellij.lang.documentation.linkHandler.KiteSignatureInfoLinkHandler;
import com.kite.intellij.lang.documentation.linkHandler.LinkRenderContext;
import com.kite.intellij.lang.documentation.linkHandler.SignatureLinkData;
import com.kite.intellij.platform.fs.CanonicalFilePath;
import com.kite.intellij.platform.fs.CanonicalFilePathFactory;
import com.kite.intellij.settings.KiteSettings;
import com.kite.intellij.settings.KiteSettingsService;
import com.kite.intellij.ui.KiteTestUtil;
import com.kite.intellij.ui.SwingWorkerUtil;
import com.kite.intellij.ui.html.KiteContentUpdateListener;
import com.kite.intellij.ui.html.KiteHtmlTextPopup;
import com.kite.intellij.util.KiteAlarm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Manages a single signature popup in a given editor.
 * <p>
 * A signature info popup changes its content if the caret position changes or if new text is entered in the editor.
 * Both of these actions are handled by this class. It attaches listeners to the editor and makes sure that an opened popup
 * is closed if the caret moved outside of a function call.
 * <p>
 * This class also tracks the currently highlightes paramter index (see {@link #moveToNextParameter()},
 * {@link #moveToPreviousParameter()} and {@link #resetParameterIndex()}).
 * An edit event reset the manually set parameter index, a caret move does not update it.
 * We assume that a user is interested in
 * the new parameter if he edits but would like to see her/his manually chosen parameter (via prev/next)
 * if he just moves the caret around.
 */
public class SignaturePopupController implements Disposable {
    private static final Logger LOG = Logger.getInstance("#kite.ui.signaturePopup");
    private static final JBInsets POPUP_MARGIN_AROUND = new JBInsets(15, 15, 15, 15);
    private static final Key<Boolean> POSITION_ABOVE = Key.create("kite.sigPanelAbove");

    private final KiteHtmlTextPopup htmlTextPopup;
    private final Editor editor;
    private final LightweightHint hint;
    private final HintHint hintHint;
    private final PsiFile file;
    private final CaretListener caretListener;
    private final Alarm updatePopupAlarm;
    private final KiteSignatureInfoLinkHandler signatureLinkHandler;
    private final KiteContentUpdateListener htmlContentUpdateListener;
    private volatile boolean disposed = false;
    private int activeParameterIndex = -1;
    private int maxParameterIndex = 0;
    private boolean resetParamIndex = true;
    private boolean inKwargs;
    private boolean hasKwarg;
    private volatile boolean unitTestVisible = false;

    SignaturePopupController(Editor editor, PsiFile file, DocumentBuilderFactory docBuilderFactory) {
        this.editor = editor;
        this.file = file;

        Project project = editor.getProject();
        if (editor instanceof EditorImpl) {
            Disposer.register(((EditorImpl) editor).getDisposable(), this);
        } else if (project != null) {
            Disposer.register(project, this);
        } else {
            LOG.error("signature popup controller not registered with disposable parent");
        }

        DocumentBuilder builder = null;
        try {
            builder = docBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ignore) {
        }

        KiteDocumentationRenderer renderer = KiteDocumentationRendererService.getInstance(file.getProject()).getDetailedRenderer();
        htmlTextPopup = new KiteHtmlTextPopup(renderer, editor, builder, this);
        htmlTextPopup.init(false);
        htmlTextPopup.setBorder(BorderFactory.createEmptyBorder());

        hint = new LightweightHint(htmlTextPopup);
        hint.setForceHideShadow(false);
        hint.setForceShowAsPopup(true);

        hintHint = createHintHint(editor, hint);

        //defaults to the swing thread
        updatePopupAlarm = new Alarm(this);

        //the editor listener is disposed together with this popup controller
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(DocumentEvent e) {
                cancelAlarm();
                addAlarm();
            }
        }, this);

        caretListener = new CaretListener() {
            @Override
            public void caretPositionChanged(CaretEvent e) {
                cancelAlarm();
                //fixme hide instantly if the param info is not available here
                addAlarm();
            }
        };
        editor.getCaretModel().addCaretListener(caretListener);

        Optional<KiteSignatureInfoLinkHandler> optionalLinkHandler = LinksHandlers.findMatchingLinkHandler(KiteSignatureInfoLinkHandler.class, LinksHandlers.all());
        if (!optionalLinkHandler.isPresent()) {
            throw new IllegalArgumentException("Could not find link handler for KiteSignatureInfoLink");
        }

        signatureLinkHandler = optionalLinkHandler.get();

        //this link listener forces an update of the content and updates the active parameter by reading the link properties
        htmlContentUpdateListener = (linkData, linkHandler) -> {
            positionAndResize(true);

            if (signatureLinkHandler.supports(linkData)) {
                SignatureLinkData signatureLinkData = (SignatureLinkData) linkData;
                SignaturePopupController.this.activeParameterIndex = signatureLinkData.getArgIndex().orElse(0);
                SignaturePopupController.this.inKwargs = signatureLinkData.isInKwargs();
            }
        };

        this.htmlTextPopup.registerContentUpdateListener(htmlContentUpdateListener);

        PropertyChangeListener lookupListener = evt -> {
            if (isDisposed()) {
                return;
            }

            if (LookupManager.PROP_ACTIVE_LOOKUP.equals(evt.getPropertyName())) {
                LookupImpl lookup = (LookupImpl) evt.getNewValue();
                if (lookup != null) {
                    adjustPositionForLookup();
                }
            }
        };
        LookupManager.getInstance(file.getProject()).addPropertyChangeListener(lookupListener, this);
    }

    public void show(LinkRenderContext linkRenderContext, SignatureLinkData linkData, Calls signatureInfo) {
        if (isPopupVisible()) {
            updateComponent(linkRenderContext, linkData, signatureInfo);
            return;
        }

        int[] max = KiteConstants.SIGNATURE_POPUP_PREFERRED_WIDTH_PIXELS;
        htmlTextPopup.setMinimumSize(new JBDimension(max[0], KiteConstants.SIGNATURE_POPUP_MIN_HEIGHT_PIXELS));
        htmlTextPopup.setMaximumSize(new JBDimension(max[max.length - 1], KiteConstants.SIGNATURE_POPUP_MAX_HEIGHT_PIXELS));

        HintManagerImpl hintManager = HintManagerImpl.getInstanceImpl();
        if (hintManager == null) {
            unitTestVisible = false;
            return;
        }

        //showEditorHint modifies the point argument, this we have to pass a copy
        hintManager.showEditorHint(hint, editor, new Point(hintHint.getOriginalPoint()), HintManager.HIDE_BY_ESCAPE | HintManager.UPDATE_BY_SCROLLING, 0, false, hintHint);
        unitTestVisible = true;

        //the initial call doesn't need a forced position update because this.hintHint already has the hacked position.
        //this is better because otherwise the hint would be moved 2 times (initial show at pos, hacked move at pos.translated(1,0) and show at pos)
        updateComponent(linkRenderContext, linkData, signatureInfo);
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            unitTestVisible = false;
            editor.getCaretModel().removeCaretListener(caretListener);
            htmlTextPopup.removeContentUpdateListener(htmlContentUpdateListener);
            POSITION_ABOVE.set(editor, null);
            //the document listener is automatically removed
        }
    }

    public boolean isDisposed() {
        return disposed;
    }

    public boolean isPopupVisible() {
        return hint.isVisible() || (unitTestVisible && ApplicationManager.getApplication().isUnitTestMode());
    }

    public void moveToNextParameter() {
        if ((activeParameterIndex + 1) < maxParameterIndex) {
            activeParameterIndex++;
            updateComponent();
        } else if (hasKwarg && !inKwargs) {
            inKwargs = true;
            updateComponent();
        }
    }

    public void moveToPreviousParameter() {
        if (inKwargs && hasKwarg) {
            activeParameterIndex = maxParameterIndex - 1;
            inKwargs = false;
            updateComponent();
        } else if (activeParameterIndex > 0) {
            activeParameterIndex--;
            updateComponent();
        }
    }

    /**
     * Reset the parameter position override. Prev/Next set the position, an edit event must reset the parameter position
     * to the index returned by the Kite response.
     */
    public void resetParameterIndex() {
        resetParamIndex = true;
    }

    public void closePopup() {
        if (hint.isVisible()) {
            unitTestVisible = false;
            hint.hide();
        }
    }

    @TestOnly
    public void flushAlarms() {
        KiteAlarm.flush(updatePopupAlarm);
        try {
            KiteTestUtil.runInEdtAndWait(() -> {
            });
        } catch (Throwable throwable) {
            //ignored
        }
    }

    @Nullable
    LinkRenderContext createLinkRenderContext() {
        CanonicalFilePath filePath = CanonicalFilePathFactory.getInstance().createFor(editor, CanonicalFilePathFactory.Context.Event);
        if (filePath == null) {
            return null;
        }

        //content override
        int offset = editor.getCaretModel().getOffset();
        String content = FileEditorUtil.contentOf(editor);

        //see if we can override
        Character typed = SignatureTypedHandler.getLastTypedCharacter(editor, false);
        if (typed != null && (typed.equals('(') || typed.equals(','))) {
            if (offset > 0 && offset < content.length()) {
                content = content.substring(0, offset) + typed + content.substring(offset + 1);
                offset++;
            } else if (offset == content.length()) { //end of file
                content = content + typed;
                offset++;
            }
        }

        return new LinkRenderContext(editor.getProject(), filePath, content, offset);
    }

    @TestOnly
    int getActiveParameterIndex() {
        return activeParameterIndex;
    }

    @TestOnly
    boolean isInKwargs() {
        return inKwargs;
    }

    Editor getEditor() {
        return editor;
    }

    /**
     * Retrieves the signature data from Kite. If the connection or request failed then a {@link FallbackToOriginalException} is thrown
     * if the response code is not a 404 (if there was any).
     * <p>
     * If an error occurred then a currently visible popup is hidden and the current instance is disposed.
     *
     * @param linkRenderContext
     * @param linkData
     * @return
     * @throws FallbackToOriginalException
     */
    @Nullable
    Calls requestSignatureInfo(LinkRenderContext linkRenderContext, SignatureLinkData linkData) throws FallbackToOriginalException {
        try {
            Optional<Calls> signatureInfoOpt = signatureLinkHandler.rawResponseData(linkData, linkRenderContext);

            if (signatureInfoOpt.isPresent()) {
                return signatureInfoOpt.get();
            }
        } catch (KiteHttpException e) {
            //no signature was found at the given offset, return without falling back to the original action
            ApplicationManager.getApplication().invokeLater(hint::hide);
            Disposer.dispose(this);

            if (e instanceof HttpStatusException && ((HttpStatusException) e).isNotFoundError404()) {
                return null;
            }

            throw new FallbackToOriginalException("request/connection error requesting signature info", e);
        }

        //either an timeout or interrupted exception was thrown or no signature was found the given offset, return without falling back to the original action
        ApplicationManager.getApplication().invokeLater(hint::hide);
        Disposer.dispose(this);
        return null;
    }

    @Nonnull
    protected SignatureLinkData createLinkData(boolean automaticPopupMode) {
        OptionalInt argIndex = activeParameterIndex >= 0 ? OptionalInt.of(activeParameterIndex) : OptionalInt.empty();
        KiteSettings state = KiteSettingsService.getInstance().getState();
        return new SignatureLinkData(argIndex, inKwargs, state.showKwargs, state.showPopularPatterns, automaticPopupMode);
    }

    private void adjustPositionForLookup() {
        if (!hint.isVisible() && !ApplicationManager.getApplication().isUnitTestMode() || editor.isDisposed()) {
            Disposer.dispose(this);
            return;
        }

        positionAndResize(false);
    }

    private HintHint createHintHint(Editor editor, LightweightHint hint) {
        assert editor != null;
        assert file != null;

        short constraint = computeShowAbove(editor) ? HintManager.ABOVE : HintManager.UNDER;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Initial position: " + (constraint == HintManager.ABOVE ? "ABOVE" : "BELOW"));
        }

        HintHint h = HintManagerImpl.createHintHint(editor, calcPopupPosition(editor, htmlTextPopup.getMinimumSize(), file, getExternalComponent(editor)), hint, constraint, false);
        h.setBorderInsets(JBUI.insets(0));
        h.setExplicitClose(true);
        h.setShowImmediately(true);
        h.setAnimationEnabled(false);
        h.setMayCenterPosition(false);
        h.setAwtTooltip(false);
        h.setContentActive(true);

        return h;
    }

    /**
     * Returns the position for the popup for the current caret position in the editor for the given hint.
     *
     * @return The position where the hint should be displayed, relative to [0,0] of the editor content component
     */
    private static Point calcPopupPosition(Editor editor, Dimension popupDimension, PsiFile file, JComponent externalComponent) {
        PsiElement anchorElement = findArgumentList(file, editor.getCaretModel().getOffset());
        TextRange range = anchorElement != null ? anchorElement.getTextRange() : TextRange.from(editor.getCaretModel().getOffset(), 1);

        //move the popup below the last line if the invocation spans multiple lines
        LogicalPosition logicalPosition = editor.offsetToLogicalPosition(range.getStartOffset());
        if (FileEditorUtil.isSpanningMulitpleLines(editor, range)) {
            logicalPosition = new LogicalPosition(editor.offsetToLogicalPosition(range.getEndOffset()).line, logicalPosition.column);
        }

        RelativePoint relativePoint = new RelativePoint(editor.getContentComponent(), editor.logicalPositionToXY(logicalPosition));
        Point targetPoint = relativePoint.getPoint(externalComponent);
        Point targetScreenPoint = relativePoint.getScreenPoint();

        boolean positionAbove = computeShowAbove(editor);

        //show above the start offset of the invocation if it doesn't fit below.
        boolean fitsAbove = (targetScreenPoint.y - popupDimension.height - POPUP_MARGIN_AROUND.top - POPUP_MARGIN_AROUND.bottom) >= 0;
        boolean fitsBelow = (targetPoint.y + popupDimension.height + POPUP_MARGIN_AROUND.top + POPUP_MARGIN_AROUND.bottom) <= externalComponent.getHeight();

        // if we show the panel above the current line we want to allow it to overlap the main window / editor component
        // we're not doing that yet for the position below
        boolean adjustToEditorRect;
        if (!positionAbove && fitsBelow || positionAbove && !fitsAbove) {
            // this is the position below the current line
            targetPoint.translate(0, editor.getLineHeight() + JBUI.scale(3));
            adjustToEditorRect = true;
        } else {
            // this is the position above the current line
            Point posFirstLine = editor.logicalPositionToXY(editor.offsetToLogicalPosition(range.getStartOffset()));
            targetPoint = new RelativePoint(editor.getContentComponent(), posFirstLine).getPoint(externalComponent);
            targetPoint.translate(0, -popupDimension.height - JBUI.scale(3));
            adjustToEditorRect = false;
        }

        //flip if it doesn't on the x axis of the screen
        Rectangle movedTarget = new Rectangle(targetPoint, popupDimension);
        if (adjustToEditorRect) {
            // adjust the panel to fit into the editor component
            // the rect must not be moved up to cover the current line
            // if the adjusted rect contains the target point then we're not applying the adjustment
            // because it's now covering the current line
            Rectangle adjustedRect = new Rectangle(movedTarget);
            ScreenUtil.moveToFit(adjustedRect, new Rectangle(0, 0, externalComponent.getWidth(), externalComponent.getHeight()), POPUP_MARGIN_AROUND);
            // the rect always contains targetPoint.
            // If it was moved up it will contains the pixel above targetPoint,
            // which is already part of the current line
            // if that's the case we're not applying the adjustment
            if (!adjustedRect.contains(new Point(targetPoint.x, targetPoint.y - 1))) {
                movedTarget = adjustedRect;
            }
        }
        return movedTarget.getLocation();
    }

    private static boolean computeShowAbove(Editor editor) {
        boolean lookupVisible = LookupManager.getActiveLookup(editor) != null;
        boolean result = lookupVisible || POSITION_ABOVE.get(editor) == Boolean.TRUE;

        POSITION_ABOVE.set(editor, result);
        return result;
    }

    @Nullable
    private static PsiElement findArgumentList(PsiFile file, int offset) {
        for (KiteDocPsiLocator psiLocator : KiteDocPsiLocator.EP_NAME.getExtensions()) {
            if (psiLocator.supports(file)) {
                PsiElement newElement = psiLocator.findArgumentList(file, offset);
                if (newElement != null) {
                    return newElement;
                }
            }
        }

        return null;
    }

    private int findMaxParameterCount(Calls signatureInfo) {
        int maxParams = 0;
        for (Call call : signatureInfo.getCalls()) {
            Detail detail = call.getCallee().getDetail();

            PythonFunctionDetails functionDetails = null;
            if (detail instanceof PythonTypeDetails) {
                functionDetails = ((PythonTypeDetails) detail).getConstructor();
            } else if (detail instanceof PythonFunctionDetails) {
                functionDetails = (PythonFunctionDetails) detail;
            }

            if (functionDetails != null) {
                maxParams = Math.max(maxParams, functionDetails.getParameters().length);
                if (functionDetails.hasVararg()) {
                    maxParams++;
                }
            }
        }

        return maxParams;
    }

    private void addAlarm() {
        if (isDisposed()) {
            return;
        }

        resetParameterIndex();

        if (isPopupVisible() && !updatePopupAlarm.isDisposed()) {
            updatePopupAlarm.addRequest(() -> {
                try {
                    updateComponent();
                } catch (FallbackToOriginalException e) {
                    //we can't handle this here because it's not an action invocation but triggered by keyboard
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("HTTP request failed/interrupted while updating signature popup", e);
                    }
                }
            }, KiteConstants.SIGNATURE_UPDATE_DELAY_MILLIS, ModalityState.stateForComponent(editor.getComponent()));
        }
    }

    private void cancelAlarm() {
        updatePopupAlarm.cancelAllRequests();
    }

    private void updateComponent() {
        LinkRenderContext linkRenderContext = createLinkRenderContext();
        if (linkRenderContext == null) {
            //may happen if the action is invoked for an unsupported file (the kite action delegation should take care of this, though)
            Disposer.dispose(this);
            return;
        }

        SignatureLinkData linkData = createLinkData(false);

        // retrieve the signature information in the background, updateComponent() is called on the EDT
        SwingWorkerUtil.compute(
                () -> requestSignatureInfo(linkRenderContext, linkData),
                (signatureInfo) -> {
                    if (signatureInfo != null) {
                        updateComponent(linkRenderContext, linkData, signatureInfo);
                    } else {
                        LOG.debug("Error while retrieving signature info");
                    }
                });
    }

    /**
     * Updates the position and content of the popup component. It must be already visible on the screen.
     */
    private void updateComponent(LinkRenderContext linkRenderContext, SignatureLinkData linkData, Calls signatureInfo) {
        boolean unitTestMode = ApplicationManager.getApplication().isUnitTestMode();
        if (!unitTestMode && !hint.isVisible()) {
            Disposer.dispose(this);
            return;
        }

        if (signatureInfo != null) {
            hasKwarg = containsKwarg(signatureInfo);
            maxParameterIndex = findMaxParameterCount(signatureInfo);

            final SignatureLinkData finalLinkData;
            if (resetParamIndex && signatureInfo.getFirstCall() != null) {
                resetParamIndex = false;

                activeParameterIndex = signatureInfo.getFirstCall().getArgIndex();
                inKwargs = signatureInfo.getFirstCall().isInKwargs();
                finalLinkData = new SignatureLinkData(OptionalInt.of(activeParameterIndex), inKwargs, linkData.isExpandKwargs(), linkData.isExpandPopularPatterns(), linkData.isAutomaticPopupMode());
            } else {
                finalLinkData = linkData;
            }

            SwingWorkerUtil.compute(
                    () -> signatureLinkHandler.render(Optional.of(signatureInfo), finalLinkData, linkRenderContext, htmlTextPopup.getRenderer()),
                    (result) -> {
                        if (result != null && result.isPresent()) {
                            htmlTextPopup.setXHTML(result.get());

                            if (!unitTestMode) {
                                positionAndResize(true);
                            }
                        } else {
                            LOG.warn("Error while rendering signature information");
                        }
                    }
            );
        }
    }

    /**
     * Popup position hack. We user {@link HintManagerImpl#showEditorHint(LightweightHint, Editor, Point, int, int, boolean, HintHint)} to
     * set the initial position of the popup. After that we let the html component compute its height (it needs to be visible to do that)
     * and then update the popup with the new dimensions.
     * We call {@link LightweightHint#updateLocation(int, int)} to update the popup tooltip with the resized dimensions. The problem is that
     * it tries to be smart and skips the update if the position is still the same. Therefore we initially show the popup with a position moved 1px to the bottom
     * edge and revert this in {@link #updateComponent()} to make the popup hint repaint with the resized popup content.
     */
    private void positionAndResize(boolean forceResize) {
        if (forceResize) {
            //calculate the content height after a relayout, we have a set of possible widths and the layout call updates the height
            Dimension optimalSize = htmlTextPopup.computeOptimalDimension(KiteConstants.SIGNATURE_POPUP_PREFERRED_WIDTH_PIXELS);

            if (optimalSize.height > 0 && optimalSize.width > 0) {
                Dimension max = htmlTextPopup.getMaximumSize();

                htmlTextPopup.setPreferredSize(new Dimension(Math.min(optimalSize.width, max.width), Math.min(optimalSize.height, max.height)));
                htmlTextPopup.revalidate();
                htmlTextPopup.repaint();

                hint.pack();
            }
        }

        Point point = calcPopupPosition(editor, htmlTextPopup.getPreferredSize(), file, getExternalComponent(editor));
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("calcPopupPosition: %s, forced: %b [%d]", point.getLocation(), forceResize, System.identityHashCode(this)));
        }

        if (hint.isVisible()) {
            hint.updateLocation(point.x, point.y);
        }
    }

    private boolean containsKwarg(Calls signatureInfo) {
        for (Call call : signatureInfo.getCalls()) {
            Detail detail = call.getCallee().getDetail();
            if (detail instanceof PythonFunctionDetails && ((PythonFunctionDetails) detail).hasKwarg()) {
                return true;
            }

            if (detail instanceof PythonTypeDetails && ((PythonTypeDetails) detail).hasConstructor() && ((PythonTypeDetails) detail).getConstructor().hasKwarg()) {
                return true;
            }
        }

        return false;
    }

    /**
     * This must be the same as {@link HintManagerImpl#getExternalComponent(Editor)}.
     *
     * @param editor
     * @return
     */
    @NotNull
    private static JComponent getExternalComponent(@NotNull Editor editor) {
        JComponent externalComponent = editor.getComponent();
        JRootPane rootPane = externalComponent.getRootPane();
        if (rootPane == null) {
            return externalComponent;
        }
        JLayeredPane layeredPane = rootPane.getLayeredPane();
        return layeredPane != null ? layeredPane : rootPane;
    }
}

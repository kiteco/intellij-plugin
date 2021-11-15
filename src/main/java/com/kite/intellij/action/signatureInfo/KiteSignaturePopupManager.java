package com.kite.intellij.action.signatureInfo;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.kite.intellij.action.FallbackToOriginalException;
import com.kite.intellij.backend.model.Calls;
import com.kite.intellij.lang.documentation.linkHandler.LinkRenderContext;
import com.kite.intellij.lang.documentation.linkHandler.SignatureLinkData;
import com.kite.intellij.ui.SwingWorkerUtil;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * This class handles the signature popups shown in an editor.
 * <p>
 * The signature popup is a lightweight hint, i.e. it does not receive focus and is in a balloon popup.
 * The signature popup is updated on editor changes and will be hidden when the user preses escape or clicks with the mouse
 * at another location.
 *
 * @see com.intellij.codeInsight.hint.ShowParameterInfoHandler
 * @see com.intellij.codeInsight.hint.ShowParameterInfoContext
 */
public class KiteSignaturePopupManager {
    private static final Logger LOG = Logger.getInstance("#kite.ui.signature");

    private final DocumentBuilderFactory documentBuilderFactory;

    KiteSignaturePopupManager() {
        documentBuilderFactory = createDocumentBuilderFactory();
    }

    public static DocumentBuilderFactory createDocumentBuilderFactory() {
        DocumentBuilderFactory factory;
        factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(false);
        factory.setExpandEntityReferences(false);
        factory.setXIncludeAware(false);
        return factory;
    }

    @Nonnull
    public static KiteSignaturePopupManager getInstance(Project project) {
        return ServiceManager.getService(project, KiteSignaturePopupManager.class);
    }

    @TestOnly
    public static void flushEvents(Editor editor) {
        SignaturePopupController visibleController = SignatureInfoEditorTracker.currentlyVisibleController(editor);
        if (visibleController != null) {
            visibleController.flushAlarms();
        }
    }

    public void showAutomaticSignatureInfo(Editor editor, PsiFile file, Consumer<Throwable> fallbackHandler) {
        showSignatureInfo(editor, file, true, fallbackHandler);
    }

    void showSignatureInfo(Editor editor, PsiFile file, Consumer<Throwable> fallbackHandler) {
        showSignatureInfo(editor, file, false, fallbackHandler);
    }

    protected void showSignatureInfo(Editor editor, PsiFile file, boolean automaticPopupMode, @Nullable Consumer<Throwable> fallbackHandler) {
        if (editor.isDisposed() || !file.isValid()) {
            return;
        }

        ApplicationManager.getApplication().assertIsDispatchThread();

        SignaturePopupController popupController = findPopupController(editor, file);
        LinkRenderContext linkRenderContext = popupController.createLinkRenderContext();
        SignatureLinkData linkData = popupController.createLinkData(automaticPopupMode);

        if (linkRenderContext == null) {
            return;
        }

        //if we have a fallback handler then we're able to perform the retrieval of the signature info in the background
        //otherwise we have to perform it in the current thread
        if (fallbackHandler == null) {
            Calls signatureInfo = popupController.requestSignatureInfo(linkRenderContext, linkData);
            try {
                if (signatureInfo != null) {
                    processCalls(popupController, signatureInfo, linkRenderContext, linkData);
                }
            } catch (Exception e) {
                processError(popupController, null, e);
            }
        } else {
            SwingWorkerUtil.compute(
                    () -> popupController.requestSignatureInfo(linkRenderContext, linkData),
                    (calls) -> {
                        if (calls != null) {
                            processCalls(popupController, calls, linkRenderContext, linkData);
                        } else {
                            processError(popupController, fallbackHandler, null);
                        }
                    }
            );
        }
    }

    private void processError(SignaturePopupController popupController, Consumer<Throwable> fallbackHandler, @Nullable Throwable e) {
        popupController.closePopup();

        Throwable cause = e instanceof ExecutionException ? e.getCause() : e;

        if (fallbackHandler != null) {
            fallbackHandler.accept(cause);
        } else if (!(cause instanceof FallbackToOriginalException)) {
            LOG.warn("Error while retrieving signature information", cause);
        }
    }

    @Nonnull
    private SignaturePopupController findPopupController(Editor editor, PsiFile file) {
        SignaturePopupController currentlyVisibleController = SignatureInfoEditorTracker.currentlyVisibleController(editor);
        if (currentlyVisibleController != null) {
            return currentlyVisibleController;
        }

        SignaturePopupController popupController = new SignaturePopupController(editor, file, documentBuilderFactory);
        SignatureInfoEditorTracker.register(editor, popupController);
        return popupController;
    }

    private void processCalls(SignaturePopupController popupController, Calls signatureInfo, LinkRenderContext linkRenderContext, SignatureLinkData linkData) {
        if (signatureInfo == null) {
            popupController.closePopup();
            return;
        }

        // re-use any existing signature info popup,
        // this may happen if the action is called again while a popup is still visible
        popupController.show(linkRenderContext, linkData, signatureInfo);
    }
}

package com.kite.intellij.action.signatureInfo;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.kite.intellij.lang.KiteLanguageSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * This handler is created by {@link KiteSignatureInfoAction} and opens the signature info panel in source
 * files supported by Kite.
 *
  */
public class KiteShowSignatureInfoActionHandler implements CodeInsightActionHandler, FallbackAwareAction {
    @Nullable
    private Consumer<Throwable> fallbackCallback;

    /**
     * @param editor The editor to check
     * @return true if the signature info may be displayed for the current offset in the given editor
     */
    public static boolean isAvailable(Editor editor) {
        return KiteLanguageSupport.isSupported(editor, KiteLanguageSupport.Feature.SignatureInfo);
    }

    @Override
    public void invoke(@Nonnull Project project, @Nonnull Editor editor, @Nonnull PsiFile file) {
        ApplicationManager.getApplication().assertIsDispatchThread();

        if (isAvailable(editor)) {
            PsiDocumentManager.getInstance(project).commitAllDocuments();

            KiteSignaturePopupManager.getInstance(project).showSignatureInfo(editor, file, fallbackCallback);
        }
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public synchronized void setFallbackCallback(@Nonnull Consumer<Throwable> fallbackCallback) {
        this.fallbackCallback = fallbackCallback;
    }

    @Override
    public synchronized void resetFallbackCallback() {
        this.fallbackCallback = null;
    }
}

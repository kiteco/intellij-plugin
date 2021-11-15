package com.kite.intellij.action.signatureInfo;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.kite.intellij.lang.KiteLanguageSupport;
import com.kite.intellij.lang.documentation.KiteDocumentationRendererService;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class MockKiteSignaturePopupManager extends KiteSignaturePopupManager {
    private static volatile int callCount = 0;

    public MockKiteSignaturePopupManager(@Nonnull KiteDocumentationRendererService rendererService, @Nonnull Project project) {
        super();
    }

    public static int getCallCount() {
        return callCount;
    }

    public static void reset() {
        callCount = 0;
    }

    @Override
    public void showSignatureInfo(Editor editor, PsiFile file, boolean automaticPopupMode, Consumer<Throwable> fallbackHandler) {
        if (KiteLanguageSupport.isSupported(editor, KiteLanguageSupport.Feature.BasicSupport)) {
            callCount++;
        }

        super.showSignatureInfo(editor, file, automaticPopupMode, fallbackHandler);
    }

    @Nonnull
    public static MockKiteSignaturePopupManager getInstance(Project project) {
        return (MockKiteSignaturePopupManager) ServiceManager.getService(project, KiteSignaturePopupManager.class);
    }
}

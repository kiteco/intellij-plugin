package com.kite.intellij.ui.html;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.PythonFileType;
import com.kite.intellij.action.signatureInfo.KiteSignaturePopupManager;
import com.kite.intellij.lang.documentation.KiteDocumentationRenderer;
import com.kite.intellij.lang.documentation.PebbleDocumentationRenderer;
import com.kite.intellij.lang.documentation.linkHandler.ExternalDocumentationLinkData;
import com.kite.intellij.lang.documentation.linkHandler.KiteExternalDocumentationLinkHandler;
import com.kite.intellij.lang.documentation.linkHandler.LinkRenderContext;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class KiteHtmlTextPopupTest extends KiteLightFixtureTest {
    @Test
    public void testLinkHandler() throws Exception {
        myFixture.configureByText(PythonFileType.INSTANCE, "");
        PebbleDocumentationRenderer renderer = new PebbleDocumentationRenderer();

        Editor editor = myFixture.getEditor();

        AtomicBoolean rendered = new AtomicBoolean(false);
        KiteExternalDocumentationLinkHandler linkHandler = new KiteExternalDocumentationLinkHandler() {
            @Override
            public Optional<String> render(Optional<Void> data, @Nonnull ExternalDocumentationLinkData linkData, LinkRenderContext renderContext, KiteDocumentationRenderer renderer) {
                rendered.set(true);

                return super.render(data, linkData, renderContext, renderer);
            }
        };

        Optional<String> htmlOpt = RenderUtil.renderHtml(linkHandler.asLink(new ExternalDocumentationLinkData("myId")), Optional.of(linkHandler), LinkRenderContext.create(editor), renderer, editor.getProject());
        Assert.assertFalse("The content is not applied because an external url does not render html locally", htmlOpt.isPresent());

        Assert.assertTrue("The render function must have been called, though.", rendered.get());
    }

    @Test
    public void testDisposable() throws ParserConfigurationException {
        PsiFile psiFile = myFixture.configureByText(PythonFileType.INSTANCE, "");
        PebbleDocumentationRenderer renderer = new PebbleDocumentationRenderer();

        Editor editor = myFixture.getEditor();

        KiteHtmlTextPopup popup = new KiteHtmlTextPopup(renderer, editor,
                KiteSignaturePopupManager.createDocumentBuilderFactory().newDocumentBuilder(),
                ((EditorImpl) editor).getDisposable());
        Assert.assertFalse("popup must not be dispoased after construction", popup.isDisposed());

        FileEditorManager.getInstance(getProject()).closeFile(psiFile.getVirtualFile());
        Assert.assertTrue("popup must be dispoased after the editor was disposed", popup.isDisposed());
    }
}
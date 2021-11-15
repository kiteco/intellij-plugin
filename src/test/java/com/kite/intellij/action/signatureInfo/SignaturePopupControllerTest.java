package com.kite.intellij.action.signatureInfo;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.PythonFileType;
import com.kite.intellij.lang.documentation.PebbleDocumentationRenderer;
import com.kite.intellij.test.KiteLightFixtureTest;
import com.kite.intellij.ui.html.KiteHtmlTextPopup;
import org.junit.Assert;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class SignaturePopupControllerTest extends KiteLightFixtureTest {
    public void testDisposable() throws ParserConfigurationException {
        PsiFile psiFile = myFixture.configureByText(PythonFileType.INSTANCE, "");
        Editor editor = myFixture.getEditor();

        DocumentBuilderFactory factory = KiteSignaturePopupManager.createDocumentBuilderFactory();

        PebbleDocumentationRenderer renderer = new PebbleDocumentationRenderer();
        KiteHtmlTextPopup popup = new KiteHtmlTextPopup(renderer, editor, factory.newDocumentBuilder(), ((EditorImpl)editor).getDisposable());
        SignaturePopupController controller = new SignaturePopupController(editor, psiFile, DocumentBuilderFactory.newInstance());

        Assert.assertFalse("controller must not be disposed after construction", controller.isDisposed());

        FileEditorManager.getInstance(getProject()).closeFile(psiFile.getVirtualFile());
        Assert.assertTrue("controller must be disposed after editor was disposed", controller.isDisposed());
        Assert.assertTrue("popup must be disposed after editor was disposed", popup.isDisposed());
    }
}
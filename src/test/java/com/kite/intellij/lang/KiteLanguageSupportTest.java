package com.kite.intellij.lang;

import com.intellij.ide.scratch.ScratchUtil;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

public class KiteLanguageSupportTest extends KiteLightFixtureTest {

    @Test
    public void testPythonScratchFile() {
        // 2021.2 is creating plain text scratch files by default
        if (ApplicationInfo.getInstance().getBuild().getBaselineVersion() > 201) {
            return;
        }

        VirtualFile scratchFile = createScratchFileBuffer("test.py", "import json");

        Assert.assertTrue(ScratchUtil.isScratch(scratchFile));
        Assert.assertTrue(KiteLanguageSupport.isSupported(scratchFile, KiteLanguageSupport.Feature.BasicSupport));
    }

    @Test
    public void testNonPythonScratchFile() {
        VirtualFile scratchFile = createScratchFileBuffer("test.txt", "import json");

        Assert.assertTrue(ScratchUtil.isScratch(scratchFile));
        Assert.assertFalse("non-python scratch files must not be supported", KiteLanguageSupport.isSupported(scratchFile, KiteLanguageSupport.Feature.BasicSupport));
    }

    @NotNull
    private VirtualFile createScratchFileBuffer(String filename, String content) {
        // first, create some Python file
        // then, select the text to let the new scratch file action know what to create
        // we don't want it to query for a file name

        PsiFile file = myFixture.configureByText(filename, content);
        myFixture.getEditor().getSelectionModel().setSelection(0, file.getTextLength());
        myFixture.performEditorAction("NewScratchBuffer");

        Editor current = FileEditorManager.getInstance(getProject()).getSelectedTextEditor();
        Assert.assertNotNull(current);
        Assert.assertNotEquals(myFixture.getEditor(), current);

        VirtualFile scratchFile = FileDocumentManager.getInstance().getFile(current.getDocument());
        Assert.assertNotNull(scratchFile);
        return scratchFile;
    }
}
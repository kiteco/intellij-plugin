package com.kite.intellij.lang.python;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.DebugUtil;
import com.jetbrains.python.PythonFileType;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nullable;

public class PythonDocPsiLocatorTest extends KiteLightFixtureTest {
    @Test
    public void testSimple() throws Exception {
        assertValid("my(<caret>)");
        assertValid("my(1,2<caret>)");

        assertValid("my()<caret>", "()");

        assertValid("myModule.my(<caret>)");
        assertValid("myModule.my(1, 2<caret>, 3)");
    }

    @Test
    public void testPrint() throws Exception {
        assertValid("print(1,2<caret>)");
        assertValid("print(<caret>)");
        assertValid("print()<caret>", "()");
    }

    @Test
    public void testAbs() throws Exception {
        assertValid("abs(<caret>)");
        assertValid("abs(-1<caret>)");
    }

    @Test
    public void testWithErrorMarker() throws Exception {
        assertNotValid("my(\"a\"=<caret>, b=123)");
        assertNotValid("my(\"a\"=<caret>)");
    }

    private void assertValid(String fileContent) {
        assertValid(fileContent, null);
    }

    private void assertValid(String fileContent, @Nullable String expectedElementContent) {
        PsiFile file = myFixture.configureByText(PythonFileType.INSTANCE, fileContent);

        PsiElement argumentList = new PythonDocPsiLocator().findArgumentList(file, myFixture.getCaretOffset());
        Assert.assertNotNull(String.format("Expected a argument list at %d: %s", myFixture.getCaretOffset(), DebugUtil.psiToString(file, true, true)), argumentList);

        if (expectedElementContent != null) {
            Assert.assertEquals("Expected matching content", expectedElementContent, argumentList.getText());
        }
    }

    private void assertNotValid(String fileContent) {
        PsiFile file = myFixture.configureByText(PythonFileType.INSTANCE, fileContent);

        PsiElement argumentList = new PythonDocPsiLocator().findArgumentList(file, myFixture.getCaretOffset());
        Assert.assertNull(String.format("Did not expect a argument list at %d: %s", myFixture.getCaretOffset(), DebugUtil.psiToString(file, true, true)), argumentList);
    }
}
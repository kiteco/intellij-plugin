package com.kite.intellij.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.psi.PsiFile;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Test;

public class KiteActionUtilsTest extends KiteLightFixtureTest {
    @Test
    public void testIsUnsupported() {
        assertSupport(false, "a.xml", "<tag></tag>");
        assertSupport(false, "a.java", "");
    }

    @Test
    public void testIsSupported() {
        assertSupport(true, "a.py", "print()");
    }

    private void assertSupport(boolean expected, String fileName, String content) {
        PsiFile file = myFixture.configureByText(fileName, content);
        AnActionEvent e = AnActionEvent.createFromDataContext("dummy", null, dataId -> {
            if (dataId.equals(CommonDataKeys.PROJECT.getName())) {
                return getProject();
            }
            if (dataId.equals(CommonDataKeys.EDITOR.getName())) {
                return myFixture.getEditor();
            }
            if (dataId.equals(CommonDataKeys.PSI_FILE.getName())) {
                return file;
            }
            return null;
        });

        assertEquals(expected, KiteActionUtils.isSupported(e));
    }
}
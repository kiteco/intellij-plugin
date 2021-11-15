package com.kite.intellij.lang;

import com.intellij.psi.PsiFile;
import com.kite.intellij.platform.fs.UnixCanonicalPath;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nonnull;

public class PythonKiteLanguageSupportTest extends KiteLightFixtureTest {
    public void testLanguageName() {
        Assert.assertEquals("unknown", KiteLanguageSupport.languageName(new UnixCanonicalPath("/home/user/test.txt")));
        Assert.assertEquals("unknown", KiteLanguageSupport.languageName(new UnixCanonicalPath("/home/user/test.TXT")));

        Assert.assertEquals("python", KiteLanguageSupport.languageName(new UnixCanonicalPath("/home/user/test.py")));
        Assert.assertEquals("python", KiteLanguageSupport.languageName(new UnixCanonicalPath("/home/user/test.PY")));
    }

    @Test
    public void testFiles() throws Exception {
        //Python
        assertSupported("test.py");

        //only *.py is supported by Kite
        assertNotSupported("test.pyw");

        //Python Stubs
        assertNotSupported("test.pyi");

        //Cython
        assertNotSupported("test.pxd");
        assertNotSupported("test.pxi");
        assertNotSupported("test.pyx");
    }

    private void assertSupported(@Nonnull String filePath) {
        PsiFile file = myFixture.configureByText(filePath, "");
        Assert.assertTrue("Kite support must be enabled for the file " + filePath, KiteLanguageSupport.isSupported(file));
    }

    private void assertNotSupported(@Nonnull String filePath) {
        PsiFile file = myFixture.configureByText(filePath, "");
        Assert.assertFalse("Kite support must be disabled for the file " + filePath, KiteLanguageSupport.isSupported(file));
    }
}
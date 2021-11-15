package com.kite.intellij.platform.exec;

import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.kite.intellij.platform.fs.CanonicalFilePathFactory;
import com.kite.intellij.platform.fs.CanonicalFilePathFactory.Context;
import com.kite.intellij.platform.fs.UnixCanonicalPath;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

public class DefaultCanonicalFilePathFactoryTest extends KiteLightFixtureTest {
    @Test
    public void testForNativeUnix() {
        if (!SystemInfo.isWindows) {
            CanonicalFilePathFactory factory = CanonicalFilePathFactory.getInstance();
            
            Assert.assertEquals("/Users/Test/test.py", factory.forNativePath("/Users/Test/test.py").asSlashDelimitedPath());
            Assert.assertEquals("/Users/Test/test.py", factory.forNativePath("/Users/Test/test.py").asOSDelimitedPath());
            Assert.assertEquals(":Users:Test:test.py", factory.forNativePath("/Users/Test/test.py").asKiteEncodedPath());
        }
    }

    @Test
    public void testForNativeWindows() {
        if (SystemInfo.isWindows) {
            CanonicalFilePathFactory factory = CanonicalFilePathFactory.getInstance();
            
            Assert.assertEquals("c:/Users/Test/test.py", factory.forNativePath("c:\\Users\\Test\\test.py").asSlashDelimitedPath());
            Assert.assertEquals("c:\\Users\\Test\\test.py", factory.forNativePath("c:\\Users\\Test\\test.py").asOSDelimitedPath());
            Assert.assertEquals(":windows:c:Users:Test:test.py", factory.forNativePath("c:\\Users\\Test\\test.py").asKiteEncodedPath());
        }
    }

    @Test
    public void testFilename() {
        Assert.assertEquals("file.py", new UnixCanonicalPath("/home/user/file.py").filename());
        Assert.assertEquals("file.py", new UnixCanonicalPath("/home/user/other/file.py").filename());
        Assert.assertEquals("file.py", new UnixCanonicalPath("file.py").filename());

        Assert.assertEquals("dir", new UnixCanonicalPath("/home/user/dir").filename());
        Assert.assertEquals("dir", new UnixCanonicalPath("dir").filename());
    }

    @Test
    public void testUnsupportedFileType() {
        CanonicalFilePathFactory factory = CanonicalFilePathFactory.getInstance();
        
        PsiFile file = myFixture.configureByText(PlainTextFileType.INSTANCE, "test");

        Assert.assertNull("Unsupported files must be rejected by default", factory.createFor(file, Context.Event));

        Assert.assertNull("Unsupported files must be rejected ", factory.createFor(file, Context.Event));
    }

    @Test
    public void testDirectory() {
        CanonicalFilePathFactory factory = CanonicalFilePathFactory.getInstance();
        
        VirtualFile directory = myFixture.configureByText(PlainTextFileType.INSTANCE, "").getVirtualFile().getParent();

        Assert.assertNull("Directories are not included in supported file types", factory.createFor(directory, Context.Event));
    }

    @Test
    public void testExtension() {
        CanonicalFilePathFactory factory = CanonicalFilePathFactory.getInstance();
        
        Assert.assertEquals("", factory.forNativePath("c:\\Users\\Test\\test").filenameExtension());
        Assert.assertEquals("", factory.forNativePath("c:\\Users\\Test\\test.").filenameExtension());

        Assert.assertEquals("py", factory.forNativePath("c:\\Users\\Test\\test.py").filenameExtension());
    }
}
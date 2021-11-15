package com.kite.intellij.platform.fs;

import com.kite.intellij.platform.fs.WindowsCanonicalPath;
import org.junit.Assert;
import org.junit.Test;

/**
 */
public class WindowsCanonicalPathTest {
    @Test
    public void testBasic() throws Exception {
        WindowsCanonicalPath path = new WindowsCanonicalPath("c:/users/userName/file.py");

        Assert.assertEquals("c:/users/userName/file.py", path.asSlashDelimitedPath());
        Assert.assertEquals("c:\\users\\userName\\file.py", path.asOSDelimitedPath());
        Assert.assertEquals(":windows:c:users:userName:file.py", path.asKiteEncodedPath());

        Assert.assertEquals(new WindowsCanonicalPath("c:/users/userName/file.py"), path);
        Assert.assertNotEquals(new WindowsCanonicalPath("c:/users/userName/otherFile.py"), path);
    }

    @Test
    public void testDirPaths() throws Exception {
        Assert.assertEquals(":windows:c", new WindowsCanonicalPath("c:").asKiteEncodedPath());
        Assert.assertEquals(":windows:c:", new WindowsCanonicalPath("c:/").asKiteEncodedPath());
    }

    @Test
    public void testFiles() throws Exception {
        Assert.assertEquals("test.py", new WindowsCanonicalPath("test.py").asSlashDelimitedPath());
        Assert.assertEquals("test.py", new WindowsCanonicalPath("test.py").asOSDelimitedPath());
        Assert.assertEquals("test.py", new WindowsCanonicalPath("test.py").asKiteEncodedPath());
    }
}
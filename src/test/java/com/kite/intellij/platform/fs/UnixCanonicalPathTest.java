package com.kite.intellij.platform.fs;

import com.kite.intellij.platform.fs.UnixCanonicalPath;
import org.junit.Assert;
import org.junit.Test;

/**
 */
public class UnixCanonicalPathTest {

    @Test
    public void testBasic() throws Exception {
        UnixCanonicalPath path = new UnixCanonicalPath("/home/users/file.py");
        Assert.assertEquals("/home/users/file.py", path.asSlashDelimitedPath());
        Assert.assertEquals("/home/users/file.py", path.asOSDelimitedPath());
        Assert.assertEquals(":home:users:file.py", path.asKiteEncodedPath());

        Assert.assertEquals(new UnixCanonicalPath("/home/users/file.py"), path);
        Assert.assertNotEquals(new UnixCanonicalPath("/home/users/otherFile.py"), path);
    }

    @Test
    public void testDirs() throws Exception {
        Assert.assertEquals(":", new UnixCanonicalPath("/").asKiteEncodedPath());
        Assert.assertEquals(":home", new UnixCanonicalPath("/home").asKiteEncodedPath());
    }

    @Test
    public void testFiles() throws Exception {
        Assert.assertEquals("test.py", new UnixCanonicalPath("test.py").asSlashDelimitedPath());
        Assert.assertEquals("test.py", new UnixCanonicalPath("test.py").asOSDelimitedPath());
        Assert.assertEquals("test.py", new UnixCanonicalPath("test.py").asKiteEncodedPath());
    }
}
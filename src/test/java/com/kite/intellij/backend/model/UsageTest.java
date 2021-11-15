package com.kite.intellij.backend.model;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class UsageTest {
    @Test
    public void testBasic() throws Exception {
        Usage u = new Usage("code", "file.py", 10);
        Assert.assertEquals("code", u.getCode());
        Assert.assertEquals("file.py", u.getFilename());
        Assert.assertEquals(10, u.getLine());

    }

    @Test
    public void testEquals() throws Exception {
        Usage a = new Usage("code", "file.py", 10);
        Usage b = new Usage("code", "file.py", 10);
        Usage c = new Usage("code 2", "file.py", 11);

        Assert.assertEquals(a, a);
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);

        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(c, a);
    }
}
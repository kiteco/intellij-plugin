package com.kite.intellij.backend.model;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class LocationTest {
    @Test
    public void testBasic() throws Exception {
        Location l = Location.of("file", 10);
        Assert.assertEquals("file", l.getFilePath());
        Assert.assertEquals(10, l.getLineNumber());
    }

    @Test
    public void testEquals() throws Exception {
        Location a = Location.of("file", 10);
        Location b = Location.of("file", 10);
        Location c = Location.of("file other", 10);

        Assert.assertEquals(a, a);
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);

        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(c, a);
    }
}
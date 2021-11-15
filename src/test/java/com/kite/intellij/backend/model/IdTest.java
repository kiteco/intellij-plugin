package com.kite.intellij.backend.model;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class IdTest {
    @Test
    public void testGetValue() throws Exception {
        Id id = Id.of("value");
        Assert.assertEquals("value", id.getValue());

        //char sequence equal
        Assert.assertTrue("value".contentEquals(id));
    }

    @Test
    public void testEquals() throws Exception {
        Id a = Id.of("value");
        Id b = Id.of("value");
        Id c = Id.of("other value");

        Assert.assertEquals(a, a);
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);
        Assert.assertEquals(c, c);

        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(c, a);
    }
}
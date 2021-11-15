package com.kite.intellij.backend.model;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class BaseTest {
    @Test
    public void testBasic() throws Exception {
        Base b = new Base(Id.of("myId"), "myName", "myModule", Id.of("myModule"));

        Assert.assertEquals("myId", b.getId().getValue());
        Assert.assertEquals("myName", b.getName());
        Assert.assertEquals("myModule", b.getModule());
        Assert.assertEquals("myModule", b.getModuleId().getValue());
    }

    @Test
    public void testEquals() throws Exception {
        Base a = new Base(Id.of("myId"), "myName", "myModule", Id.of("myModule"));
        Base b = new Base(Id.of("myId"), "myName", "myModule", Id.of("myModule"));
        Base c = new Base(Id.of("myId other"), "myName other", "myModule", Id.of("myModule"));

        Assert.assertEquals(a, a);
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);
        Assert.assertEquals(c, c);

        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(c, a);
    }
}
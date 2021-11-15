package com.kite.intellij.backend.model;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class ExampleTest {

    @Test
    public void testBasic() throws Exception {
        Example e = new Example(Id.of("example1"), "My example");
        Assert.assertEquals("example1", e.getId().getValue());
        Assert.assertEquals("My example", e.getTitle());
    }

    @Test
    public void testEquals() throws Exception {
        Example a = new Example(Id.of("example1"), "My example");
        Example b = new Example(Id.of("example1"), "My example");
        Example c = new Example(Id.of("example2"), "My other example");

        Assert.assertEquals(a, a);
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);

        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(c, a);
    }
}
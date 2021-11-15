package com.kite.intellij.backend.model;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class SymbolTest {
    @Test
    public void testBasic() throws Exception {
        Symbol s = new Symbol(Id.of("id"), "name", null, null);

        Assert.assertEquals("id", s.getId().getValue());
        Assert.assertEquals("name", s.getName());
        Assert.assertEquals(null, s.getNamespace());
        Assert.assertEquals(null, s.getValues());
    }

    @Test
    public void testEquals() throws Exception {
        Symbol a = new Symbol(Id.of("id"), "name", null, null);
        Symbol b = new Symbol(Id.of("id"), "name", null, null);
        Symbol c = new Symbol(Id.of("id other"), "name", null, null);

        Assert.assertEquals(a, a);
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);

        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(c, a);
    }

    @Test
    public void testNullValues() throws Exception {
        Symbol symbol = new Symbol(Id.of("id"), "name", null, null);
        Assert.assertFalse(symbol.hasValues()); //must not throw a NPE
    }
}
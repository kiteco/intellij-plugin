package com.kite.intellij.backend.model;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class SymbolExtTest {
    @Test
    public void testBasic() throws Exception {
        SymbolExt s = new SymbolExt(Id.of("id"), "name", "qname", null, null, "");

        Assert.assertEquals("id", s.getId().getValue());
        Assert.assertEquals("name", s.getName());
        Assert.assertEquals("qname", s.getQualifiedName());
        Assert.assertEquals(null, s.getNamespace());
        Assert.assertEquals(null, s.getValues());
        Assert.assertEquals("", s.getSynopsis());
    }

    @Test
    public void testEquals() throws Exception {
        SymbolExt a = new SymbolExt(Id.of("id"), "name", "qname", null, null, "");
        SymbolExt b = new SymbolExt(Id.of("id"), "name", "qname", null, null, "");
        SymbolExt c = new SymbolExt(Id.of("id other"), "name", "qname", null, null, "");

        Assert.assertEquals(a, a);
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);

        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(c, a);
    }

    @Test
    public void testNullValues() throws Exception {
        SymbolExt symbolExt = new SymbolExt(Id.of("id"), "name", null, null, null, "synaopsis");
        Assert.assertFalse(symbolExt.hasValues()); //must not throw a NPE
    }
}
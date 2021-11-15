package com.kite.intellij.backend.model;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class ValueTest {
    @Test
    public void testBasic() throws Exception {
        Value a = new Value(Id.of("id"), Kind.Module, "repr", "type", "typeId", null);

        Assert.assertEquals("id", a.getId().getValue());
        Assert.assertEquals(Kind.Module, a.getKind());
        Assert.assertEquals("repr", a.getRepresentation());
        Assert.assertEquals("type", a.getType());
        Assert.assertEquals("typeId", a.getTypeId());
        Assert.assertEquals(0, a.getComponents().length);
        Assert.assertFalse(a.hasComponents());
    }

    @Test
    public void testEquals() throws Exception {
        Value a = new Value(Id.of("id"), Kind.Module, "repr", "type", "typeId", null);
        Value b = new Value(Id.of("id"), Kind.Module, "repr", "type", "typeId", null);
        Value c = new Value(Id.of("id other"), Kind.Module, "repr", "type", "typeId", null);

        Assert.assertEquals(a, a);
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);
        Assert.assertEquals(c, c);

        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(c, a);
    }
}
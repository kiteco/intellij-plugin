package com.kite.intellij.backend.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 */
public class ValueExtTest {
    @Test
    public void testBase() throws Exception {
        ValueExt a = new ValueExt(Id.of("id"), Kind.Module, "repr", "type", "typeId", null, "synopsis", null, null);

        assertEquals("id", a.getId().getValue());
        assertEquals(Kind.Module, a.getKind());
        assertEquals("repr", a.getRepresentation());
        assertEquals("type", a.getType());
        assertEquals("typeId", a.getTypeId());
        assertEquals(0, a.getComponents().length);
        assertEquals("synopsis", a.getSynopsis());
        assertEquals(0, a.getBreadcrumbs().length);
        assertEquals(null, a.getDetail());
    }

    @Test
    public void testEquals() throws Exception {
        ValueExt a = new ValueExt(Id.of("id"), Kind.Module, "repr", "type", "typeId", null, "synopsis", null, null);
        ValueExt b = new ValueExt(Id.of("id"), Kind.Module, "repr", "type", "typeId", null, "synopsis", null, null);
        ValueExt c = new ValueExt(Id.of("id"), Kind.Module, "repr", "type", "typeId", null, "synopsis other", null, null);

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(c, c);

        assertNotEquals(a, c);
        assertNotEquals(c, a);
    }
}
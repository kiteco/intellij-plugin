package com.kite.intellij.backend.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 */
public class ParameterTest {
    @Test
    public void testBasic() throws Exception {
        ParameterBase p = new ParameterBase("name", null, "synopsis");

        assertEquals("name", p.getName());
        assertFalse(p.hasInferredValue());
        assertEquals("synopsis", p.getSynopsis());
    }

    @Test
    public void testDisplayType() throws Exception {
        Parameter annotated = new ParameterBase("name", new Union(new Value(Id.of("id"), Kind.Type, "int", "int", "__builtin__.int", null)), "synopsis");
        assertArrayEquals(new String[]{"int"}, annotated.getDisplayedTypes());

        Parameter annotatedMulti = new ParameterBase("name", new Union(new Value(Id.of("id"), Kind.Type, "int", "int", "__builtin__.int", null), new Value(Id.of("id"), Kind.Type, "string", "string", "__builtin__.string", null)), "synopsis");
        assertArrayEquals(new String[]{"int", "string"}, annotatedMulti.getDisplayedTypes());
    }

    @Test
    public void testEquals() throws Exception {
        Parameter a = new ParameterBase("name", null, "synopsis");
        Parameter b = new ParameterBase("name", null, "synopsis");
        Parameter c = new ParameterBase("name", null, "synopsis other");

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(c, c);

        assertNotEquals(a, c);
        assertNotEquals(c, a);
    }
}
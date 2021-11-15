package com.kite.intellij.backend.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 */
public class PythonParameterTest {
    @Test
    public void testBasic() throws Exception {
        PythonParameter p = new PythonParameter(new ParameterBase("name", null, "synopsis"), null, null, false);

        assertEquals("name", p.getName());
        assertFalse(p.hasDefaultValue());
        assertFalse(p.hasInferredValue());
        assertFalse(p.hasAnnotation());
        assertFalse(p.isKeywordOnly());
        assertEquals("synopsis", p.getSynopsis());
    }

    @Test
    public void testDisplayType() throws Exception {
        Parameter annotated = new PythonParameter(new ParameterBase("name", new Union(new Value(Id.of("id"), Kind.Type, "int", "int", "__builtin__.int", null)), "synopsis"), null, null, false);
        assertArrayEquals(new String[]{"int"}, annotated.getDisplayedTypes());

        PythonParameter annotatedMulti = new PythonParameter(new ParameterBase("name", new Union(new Value(Id.of("id"), Kind.Type, "int", "int", "__builtin__.int", null), new Value(Id.of("id"), Kind.Type, "string", "string", "__builtin__.string", null)), "synopsis"), null, null, false);
        assertArrayEquals(new String[]{"int", "string"}, annotatedMulti.getDisplayedTypes());
    }

    @Test
    public void testEquals() throws Exception {
        Parameter a = new PythonParameter(new ParameterBase("name", null, "synopsis"), null, null, false);
        Parameter b = new PythonParameter(new ParameterBase("name", null, "synopsis"), null, null, false);
        Parameter c = new PythonParameter(new ParameterBase("name", null, "synopsis other"), null, null, false);

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(c, c);

        assertNotEquals(a, c);
        assertNotEquals(c, a);
    }
}
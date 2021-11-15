package com.kite.intellij.backend.model;

import org.junit.Assert;
import org.junit.Test;

public class TypeDetailsBaseTest {
    @Test
    public void testBasics() throws Exception {
        TypeDetailsBase a = new TypeDetailsBase(10, new Symbol[]{new Symbol(Id.of("id"), "name", null, null)});
        TypeDetailsBase b = new TypeDetailsBase(10, new Symbol[]{new Symbol(Id.of("id"), "name", null, null)});
        TypeDetailsBase c = new TypeDetailsBase(10, new Symbol[]{new Symbol(Id.of("idOther"), "name", null, null)});

        Assert.assertEquals(a, a);
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);

        Assert.assertNotEquals(a, c);
    }
}
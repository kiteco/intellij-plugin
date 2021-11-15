package com.kite.intellij.backend.model;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class UnionTest {
    @Test
    public void testBasic() throws Exception {
        Union union = new Union(new Value(Id.of("id"), Kind.Module, "respr", "type", "typeId", null));

        Assert.assertFalse(union.isEmpty());
        Assert.assertEquals(1, union.size());
    }

    @Test
    public void testEquals() throws Exception {
        Union a = new Union(new Value(Id.of("id"), Kind.Module, "respr", "type", "typeId", null));
        Union b = new Union(new Value(Id.of("id"), Kind.Module, "respr", "type", "typeId", null));
        Union c = new Union(new Value(Id.of("id"), Kind.Module, "respr", "type", "typeId", null), new Value(Id.of("id"), Kind.Module, "respr", "type", "typeId", null));

        Assert.assertEquals(a, a);
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);
        Assert.assertEquals(c, c);

        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(c, a);
    }

    @Test
    public void testTypeIds() throws Exception {
        Union a = new Union(new Value(Id.of("id1"), Kind.Module, "respr", "type1", "typeId1", null), new Value(Id.of("id2"), Kind.Function, "repr2", "type2", "typeId2", null));
        Assert.assertArrayEquals(new String[]{"typeId1", "typeId2"}, a.getTypeIds());
        Assert.assertArrayEquals(new String[]{"type1", "type2"}, a.getTypes());

        Assert.assertArrayEquals(new String[0], new Union().getTypes());
        Assert.assertArrayEquals(new String[0], new Union().getTypeIds());
    }
}
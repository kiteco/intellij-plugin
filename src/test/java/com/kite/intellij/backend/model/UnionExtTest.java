package com.kite.intellij.backend.model;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class UnionExtTest {
    @Test
    public void testBasic() throws Exception {
        UnionExt union = new UnionExt(new ValueExt(Id.of("id"), Kind.Module, "respr", "type", "typeId", null, "synopsis", null, null));

        Assert.assertFalse(union.isEmpty());
        Assert.assertEquals(1, union.size());
    }

    @Test
    public void testEquals() throws Exception {
        UnionExt a = new UnionExt(new ValueExt(Id.of("id"), Kind.Module, "respr", "type", "typeId", null, "synopsis", null, null));
        UnionExt b = new UnionExt(new ValueExt(Id.of("id"), Kind.Module, "respr", "type", "typeId", null, "synopsis", null, null));
        UnionExt c = new UnionExt(new ValueExt(Id.of("id"), Kind.Module, "respr", "type", "typeId", null, "synopsis", null, null), new ValueExt(Id.of("id"), Kind.Module, "respr", "type", "typeId", null, "synopsis", null, null));

        Assert.assertEquals(a, a);
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);
        Assert.assertEquals(c, c);

        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(c, a);
    }
}
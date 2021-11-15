package com.kite.intellij.backend.model;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class TextSelectionTest {
    @Test
    public void testBasic() throws Exception {
        TextSelection t = TextSelection.create(1, 10);
        Assert.assertEquals(1, t.getStartOffset());
        Assert.assertEquals(10, t.getEndOffset());
    }

    @Test
    public void testEquals() throws Exception {
        TextSelection a = TextSelection.create(1, 10);
        TextSelection b = TextSelection.create(1, 10);
        TextSelection c = TextSelection.create(1, 20);

        Assert.assertEquals(a, a);
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);

        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(c, a);
    }
}
package com.kite.intellij.backend.model;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class TokenTest {
    @Test
    public void testBasic() throws Exception {
        Token token = new Token(0, 5, 0, 3, "syntax name", null);

        Assert.assertEquals(0, token.getBeginBytes());
        Assert.assertEquals(5, token.getEndBytes());

        Assert.assertEquals(0, token.getBeginRunes());
        Assert.assertEquals(3, token.getEndRunes());

        Assert.assertEquals("syntax name", token.getPartOfSyntax());
        Assert.assertNull(token.getSymbol());
    }

    @Test
    public void testEquals() throws Exception {
        Token a = new Token(0, 5, 0, 3, "syntax name", null);
        Token b = new Token(0, 5, 0, 3, "syntax name", null);
        Token c = new Token(10, 15, 8, 10, "syntax name", null);

        Assert.assertEquals(a, a);
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);
        Assert.assertEquals(c, c);

        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(c, a);
    }
}
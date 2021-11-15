package com.kite.intellij.backend.model;

import com.google.common.collect.Lists;
import com.kite.intellij.backend.response.HoverResponse;
import org.junit.Assert;
import org.junit.Test;

/**
 */
public class HoverResponseTest {
    @Test
    public void testBasic() throws Exception {
        SymbolExt symbolExt = new SymbolExt(Id.of("id"), "name", "qname", null, null, "");

        HoverResponse h = new HoverResponse("name", Lists.newArrayList(symbolExt), new Report(Location.of("file.py", 10), "text", "html", null, (Usage[]) null, 0));

        Assert.assertEquals("name", h.getPartOfSyntax());
        Assert.assertEquals(1, h.getSymbols().size());
        Assert.assertTrue(h.hasSymbols());

        Assert.assertEquals("file.py", h.getReport().getDefinition().getFilePath());
    }

    @Test
    public void testEquals() throws Exception {
        HoverResponse a = new HoverResponse("name", SymbolExt.EMPTY_ARRAY, null);
        HoverResponse b = new HoverResponse("name", SymbolExt.EMPTY_ARRAY, null);
        HoverResponse c = new HoverResponse("name other", SymbolExt.EMPTY_ARRAY, null);

        Assert.assertEquals(a, a);
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);

        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(c, a);
    }
}
package com.kite.intellij.backend.model;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class ReportTest {
    @Test
    public void testBasic() throws Exception {
        Report r = new Report(Location.of("file.py", 10), "text", "html", Example.EMPTY, null, 0);

        Assert.assertEquals("file.py", r.getDefinition().getFilePath());
        Assert.assertEquals("text", r.getDescriptionText());
        Assert.assertEquals("html", r.getDescriptionHtml());
        Assert.assertFalse(r.hasExamples());
        Assert.assertFalse(r.hasUsages());
    }

    @Test
    public void testEquals() throws Exception {
        Report a = new Report(Location.of("file.py", 10), "text", "html", Example.EMPTY, null, 0);
        Report b = new Report(Location.of("file.py", 10), "text", "html", Example.EMPTY, null, 0);
        Report c = new Report(Location.of("file other.py", 10), "text", "html", Example.EMPTY, null, 0);

        Assert.assertEquals(a, a);
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);

        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(c, a);
    }

    @Test
    public void testCopy() throws Exception {
        Report a = new Report(Location.of("file.py", 10), "text", "html", Example.EMPTY, null, 10);
        Report b = a.withDescriptionHtml("description html");
        Report c = a.withDescriptionText("description text");

        Assert.assertEquals("description html", b.getDescriptionHtml());
        Assert.assertEquals("text", b.getDescriptionText());
        Assert.assertEquals(10, b.getTotalUsages());

        Assert.assertEquals("description text", c.getDescriptionText());
        Assert.assertEquals("html", c.getDescriptionHtml());
        Assert.assertEquals(10, c.getTotalUsages());
    }
}
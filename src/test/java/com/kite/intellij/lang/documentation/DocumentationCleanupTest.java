package com.kite.intellij.lang.documentation;

import com.kite.intellij.backend.model.Report;
import com.kite.intellij.backend.model.SymbolExt;
import com.kite.intellij.backend.response.HoverResponse;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 */
public class DocumentationCleanupTest {
    @Test
    @Ignore
    public void testLinkCleanup() throws Exception {
        DocumentationCleanup c = new DocumentationCleanup();
        Assert.assertEquals("#id", cleanupHtml(c, "#id"));

        Assert.assertEquals("<a href=\"#id\">", cleanupHtml(c, "<a href=\"#id\">"));

        Assert.assertEquals("<a href=\"#id\">", cleanupHtml(c, "<a href=\"#id\">"));

        Assert.assertEquals("<a href=\"#matplotlib.axes._axes.Axes\" class=\"internal_link\">Axes</a>", cleanupHtml(c, "<a href=\"#matplotlib.axes._axes.Axes\" class=\"internal_link\">Axes</a>"));
    }

    private String cleanupHtml(DocumentationCleanup c, String html) {
        return DocumentationCleanup.cleanup(new HoverResponse("p", (SymbolExt[]) null, new Report("#id", html))).getReport().getDescriptionHtml();
    }
}
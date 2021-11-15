package com.kite.intellij.lang.documentation.linkHandler;

import com.kite.intellij.lang.documentation.LinksHandlers;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.OptionalInt;

/**
 */
@SuppressWarnings("unchecked")
public class KiteDocumentFileLinkHandlerTest extends KiteLightFixtureTest {
    @Test
    public void testBasics() {
        DocumentFileLinkData data = new DocumentFileLinkData("/a/b.txt", 5);
        LinkHandler<DocumentFileLinkData, ?> handler = LinksHandlers.findMachingLinkHandler(data).get();

        Assert.assertTrue(handler.supportsLink(handler.asLink(data)));
    }

    @Test
    public void testUrl() {
        Assert.assertEquals("file:/usr/share/python/test.py", LinksHandlers.asLink(new DocumentFileLinkData("/usr/share/python/test.py", OptionalInt.empty())));
        Assert.assertEquals("file:/usr/share/python/test.py?line=5", LinksHandlers.asLink(new DocumentFileLinkData("/usr/share/python/test.py", 5)));
    }
}
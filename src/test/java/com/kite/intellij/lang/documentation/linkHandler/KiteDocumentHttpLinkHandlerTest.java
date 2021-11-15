package com.kite.intellij.lang.documentation.linkHandler;

import com.jetbrains.python.PythonFileType;
import com.kite.intellij.backend.MockKiteApiService;
import com.kite.intellij.lang.documentation.LinksHandlers;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 *
 */
@SuppressWarnings("unchecked")
public class KiteDocumentHttpLinkHandlerTest extends KiteLightFixtureTest {
    @Test
    public void testBasics() {
        DocumentHttpLinkData data = new DocumentHttpLinkData("http://www.google.de");
        LinkHandler<DocumentHttpLinkData, ?> handler = LinksHandlers.findMachingLinkHandler(data).get();

        Assert.assertTrue(handler.supportsLink(handler.asLink(data)));
    }

    @Test
    public void testHttpLink() {
        Assert.assertTrue("A link handler handling http:// urls must be present", findMatchingLinkHandler("http://www.kite.com/").isPresent());
    }

    @Test
    public void testHttpsLink() {
        Assert.assertTrue("A link handler handling http:// urls must be present", findMatchingLinkHandler("https://www.kite.com/").isPresent());
    }

    private Optional<LinkHandler> findMatchingLinkHandler(String url) {
        return Arrays.stream(LinksHandlers.all()).filter(handler -> handler.supportsLink(url)).findAny();
    }
}
package com.kite.intellij.lang.documentation;

import com.kite.intellij.lang.documentation.linkHandler.*;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.OptionalInt;

@SuppressWarnings({"rawtypes", "unchecked"})
public class LinksHandlersTest extends KiteLightFixtureTest {
    /**
     * Make sure that a handler's link data is not accepted by another handler.
     */
    @Test
    public void testLinkDataHandlers() {
        KiteLinkData[] data = new KiteLinkData[]{
                new SignatureLinkData(OptionalInt.of(10), true, true, false, false),
                new DocumentFileLinkData("/home/user/file.py", 10),
                new ExternalDocumentationLinkData("id"),
                new DocumentHttpLinkData("http://www.kite.com/")
        };

        for (KiteLinkData linkData : data) {
            LinkHandler[] all = LinksHandlers.all();
            Assert.assertEquals("Link must be supported: " + linkData, 1, Arrays.stream(all).filter(handler -> handler.supports(linkData)).count());

            String url = Arrays.stream(all).filter(handler -> handler.supports(linkData)).map(handler -> handler.asLink(linkData)).findAny().orElse(null);
            Assert.assertNotNull("url must be available if there is a matching link handler", url);

            Assert.assertEquals("There must be exactly one link handler which supports the url: " + url + ", data: " + linkData, 1, Arrays.stream(all).filter(handler -> handler.supportsLink(url)).count());
        }
    }
}
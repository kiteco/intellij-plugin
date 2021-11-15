package com.kite.intellij.status;

import com.kite.intellij.test.KiteLightFixtureTest;
import com.kite.intellij.util.KiteBrowserUtil;
import org.junit.Assert;
import org.junit.Test;

public class BrowseLinkListenerTest extends KiteLightFixtureTest {
    @Test
    public void testBasics() throws Exception {
        BrowseLinkListener linkListener = new BrowseLinkListener();
        linkListener.linkSelected(null, "http://www.kite.com");

        Assert.assertEquals("http://www.kite.com", KiteBrowserUtil.openedUrls.get(0));
    }

    @Test
    public void testEmptyLink() throws Exception {
        BrowseLinkListener linkListener = new BrowseLinkListener();
        linkListener.linkSelected(null, null);

        Assert.assertEquals(0, KiteBrowserUtil.openedUrls.size());
    }
}
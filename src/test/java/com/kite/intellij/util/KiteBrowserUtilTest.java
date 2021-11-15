package com.kite.intellij.util;

import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

public class KiteBrowserUtilTest extends KiteLightFixtureTest {
    @Test
    public void testBrowseLink() throws Exception {
        Assert.assertEquals(0, KiteBrowserUtil.openedUrls.size());
        KiteBrowserUtil.browse("http://www.kite.com");

        Assert.assertEquals(1, KiteBrowserUtil.openedUrls.size());
        Assert.assertEquals("http://www.kite.com", KiteBrowserUtil.openedUrls.get(0));
    }

    @Test
    public void testBrowseUri() throws Exception {
        Assert.assertEquals(0, KiteBrowserUtil.openedUrls.size());
        KiteBrowserUtil.browse(new URI("http://www.kite.com"));

        Assert.assertEquals(1, KiteBrowserUtil.openedUrls.size());
        Assert.assertEquals("http://www.kite.com", KiteBrowserUtil.openedUrls.get(0));
    }
}
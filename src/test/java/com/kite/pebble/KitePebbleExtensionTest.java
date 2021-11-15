package com.kite.pebble;

import org.junit.Assert;
import org.junit.Test;

public class KitePebbleExtensionTest {
    @Test
    public void testFunctions() {
        KitePebbleExtension extension = new KitePebbleExtension();

        Assert.assertTrue(extension.getFunctions().containsKey("kiteInviteLink"));
        Assert.assertEquals("http://localhost:46624/redirect/invite", extension.getFunctions().get("kiteInviteLink").execute(null, null, null, 1));
    }
}
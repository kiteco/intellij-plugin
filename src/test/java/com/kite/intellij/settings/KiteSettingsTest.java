package com.kite.intellij.settings;

import org.junit.Assert;
import org.junit.Test;

public class KiteSettingsTest {
    @Test
    public void testDefaultValues() {
        KiteSettings s = new KiteSettings();

        Assert.assertEquals(true, s.paramInfoDelayEnabled);
        Assert.assertEquals(100, s.paramInfoDelayMillis);

        Assert.assertEquals(false, s.paramInfoFontSizeEnabled);
        Assert.assertEquals(14, s.paramInfoFontSize);
    }
}
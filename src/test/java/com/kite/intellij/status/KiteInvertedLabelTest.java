package com.kite.intellij.status;

import com.intellij.ui.ColorUtil;
import com.intellij.util.ui.UIUtil;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.awt.*;

@SuppressWarnings("UseJBColor")
public abstract class KiteInvertedLabelTest extends KiteLightFixtureTest {
    @Test
    @Ignore
    public void _testBasics() throws Exception {
        KiteInvertedLabel label = new KiteInvertedLabel(UIUtil.ComponentStyle.LARGE, Color.BLACK, Color.WHITE);

        Assert.assertEquals(UIUtil.ComponentStyle.LARGE, label.getComponentStyle());

        Assert.assertEquals(Color.BLACK, label.getInvertedBackgroundColor());
        Assert.assertEquals(Color.WHITE, label.getInvertedForegroundColor());
        Assert.assertEquals("The inverted color must not be used in non-inverted mode", "333333", ColorUtil.toHex(label.getForeground()));

        Assert.assertFalse(label.isEnableInvertedMode());
        label.setEnableInvertedMode(true);
        Assert.assertTrue(label.isEnableInvertedMode());

        Assert.assertEquals("The foreground color must switch in inverted mode", Color.WHITE, label.getForeground());
    }
}
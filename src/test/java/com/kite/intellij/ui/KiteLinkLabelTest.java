package com.kite.intellij.ui;

import com.google.common.collect.Sets;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

public class KiteLinkLabelTest extends KiteLightFixtureTest {
    @Test
    public void testBasics() throws Exception {
        KiteLinkLabel<String> label = new KiteLinkLabel<>("My Label", null, null, "Link Data", UIUtil.ComponentStyle.LARGE);

        Assert.assertEquals(UIUtil.ComponentStyle.LARGE, label.getComponentStyle());
        label.setComponentStyle(UIUtil.ComponentStyle.SMALL);
        Assert.assertEquals(UIUtil.ComponentStyle.SMALL, label.getComponentStyle());

        //IntelliJ 181.x uses a different color than < 181.x (new color is java.awt.Color[r=88,g=157,b=246])
        Assert.assertTrue(Sets.newHashSet("java.awt.Color[r=82,g=99,b=155]","java.awt.Color[r=88,g=157,b=246]").contains(label.getNormal().toString()));
        label.setLinkColor(JBColor.RED);
        Assert.assertEquals(JBColor.RED, label.getLinkColor());
        Assert.assertEquals("The link color must override the normal color", JBColor.RED, label.getNormal());
    }
}
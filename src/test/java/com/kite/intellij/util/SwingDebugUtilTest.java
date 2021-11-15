package com.kite.intellij.util;

import org.junit.Assert;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;

public class SwingDebugUtilTest {
    @Test
    public void testHierarchy() throws Exception {
        JPanel parent = new JPanel();
        JPanel child = new JPanel();
        parent.add(child);

        parent.setForeground(Color.BLACK);
        parent.setBackground(Color.WHITE);
        parent.setSize(new Dimension(100,100));

        child.setForeground(Color.BLACK);
        child.setBackground(Color.WHITE);
        child.setSize(new Dimension(80,80));

        Assert.assertEquals("   [javax.swing.JPanel]: {\n" +
                "      \"bounds\": java.awt.Rectangle[x=0,y=0,width=100,height=100],\n" +
                "      \"inset\": java.awt.Insets[top=0,left=0,bottom=0,right=0],\n" +
                "      \"border\": null,\n" +
                "      \"background\": java.awt.Color[r=255,g=255,b=255],\n" +
                "      \"foreground\": java.awt.Color[r=0,g=0,b=0]\n" +
                "      }\n" +
                "\n" +
                "      [javax.swing.JPanel]: {\n" +
                "         \"bounds\": java.awt.Rectangle[x=0,y=0,width=80,height=80],\n" +
                "         \"inset\": java.awt.Insets[top=0,left=0,bottom=0,right=0],\n" +
                "         \"border\": null,\n" +
                "         \"background\": java.awt.Color[r=255,g=255,b=255],\n" +
                "         \"foreground\": java.awt.Color[r=0,g=0,b=0]\n" +
                "         }\n", SwingDebugUtil.printComponentHierarchy(parent));
    }

    @Test
    public void testParent() throws Exception {
        JPanel parent = new JPanel();

        JPanel childLevel1 = new JPanel();
        parent.add(childLevel1);

        JPanel childLevel2 = new JPanel();
        childLevel1.add(childLevel2);

        Assert.assertEquals(parent, SwingDebugUtil.findTopParent(childLevel2));
    }
}
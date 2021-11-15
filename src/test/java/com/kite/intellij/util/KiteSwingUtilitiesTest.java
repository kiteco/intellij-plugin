package com.kite.intellij.util;

import org.junit.Assert;
import org.junit.Test;

import javax.swing.*;

public class KiteSwingUtilitiesTest {
    @Test
    public void testFindParent() throws Exception {
        JPanel level1 = new JPanel();
        JLabel level2 = new JLabel();
        JButton level3 = new JButton();

        level1.add(level2);
        level2.add(level3);

        Assert.assertEquals(level1, KiteSwingUtilities.findParentOfType(level3, JPanel.class));
        Assert.assertEquals(level2, KiteSwingUtilities.findParentOfType(level3, JLabel.class));
        Assert.assertEquals(null, KiteSwingUtilities.findParentOfType(level3, JButton.class));

        Assert.assertEquals(null, KiteSwingUtilities.findParentOfType(null, JButton.class));
    }
}
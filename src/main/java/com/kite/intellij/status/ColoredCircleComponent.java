package com.kite.intellij.status;

import com.intellij.util.ui.GraphicsUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Simple component which paints a circle in the center of the component using the foreground color.
 *
  */
public class ColoredCircleComponent extends JComponent {

    public ColoredCircleComponent() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        GraphicsUtil.setupAntialiasing(g);

        int padding = 0;
        int size = Math.min(getWidth(), getHeight());

        int x = padding + (getWidth() > size ? ((getWidth() - size - 2 * padding) / 2) : 0);
        int y = padding + (getHeight() > size ? ((getHeight() - size - 2 * padding) / 2) : 0);

        g.setColor(getForeground());
        g.fillOval(x, y, size - 2 * padding, size - 2 * padding);
    }
}

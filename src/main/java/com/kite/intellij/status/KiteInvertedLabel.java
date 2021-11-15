package com.kite.intellij.status;

import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.GraphicsUtil;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * A simple JBLabel extension to paint an inverted background with rounded edges if that mode
 * is enabled by a flag. This is used to paint the PRO label in the status panel.
 * <p>
 * It paints the round edges on a rounded rectangle which is fitted around the text of the label with
 * {@link #PADDING_WIDTH} and {@link #PADDING_HEIGHT}.
 *
  */
public class KiteInvertedLabel extends JBLabel {
    private static final int PADDING_WIDTH = JBUI.scale(10);
    private static final int PADDING_HEIGHT = JBUI.scale(5);

    private final Color invertedBackgroundColor;
    private final Color invertedForegroundColor;
    private boolean enableInvertedMode = true;

    public KiteInvertedLabel(@NotNull UIUtil.ComponentStyle componentStyle, Color invertedBackgroundColor, Color invertedForegroundColor) {
        super(componentStyle);

        this.invertedBackgroundColor = invertedBackgroundColor;
        this.invertedForegroundColor = invertedForegroundColor;
        setForeground(invertedForegroundColor);

        setHorizontalAlignment(CENTER);
    }

    public Color getInvertedBackgroundColor() {
        return invertedBackgroundColor;
    }

    public Color getInvertedForegroundColor() {
        return invertedForegroundColor;
    }

    public boolean isEnableInvertedMode() {
        return enableInvertedMode;
    }

    public void setEnableInvertedMode(boolean enabled) {
        if (enabled != this.enableInvertedMode) {
            this.enableInvertedMode = enabled;

            if (enabled) {
                setForeground(invertedForegroundColor);
            } else {
                setForeground(null);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();

        return new Dimension(size.width + 2 * PADDING_WIDTH, size.height + 2 * PADDING_HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (enableInvertedMode) {
            GraphicsUtil.setupAntialiasing(g);
            g.setColor(invertedBackgroundColor);

            Rectangle r = new Rectangle(getSize());
            r.x += JBUI.scale(2);
            r.y += JBUI.scale(2);
            r.width = getFontMetrics(getFont()).stringWidth(getText()) + 2 * PADDING_WIDTH;

            g.fillRoundRect(r.x, r.y, r.width - 2 * JBUI.scale(2), r.height - 2 * JBUI.scale(2), JBUI.scale(5), JBUI.scale(5));
            g.drawRoundRect(r.x, r.y, r.width - 2 * JBUI.scale(2), r.height - 2 * JBUI.scale(2), JBUI.scale(5), JBUI.scale(5));
        }

        super.paintComponent(g);
    }
}

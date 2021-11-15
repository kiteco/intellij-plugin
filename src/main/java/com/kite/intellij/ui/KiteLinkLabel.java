package com.kite.intellij.ui;

import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.ui.components.labels.LinkListener;
import com.intellij.util.ui.GraphicsUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * A drop-in replacement for {@link LinkLabel} which has an additional component style to set the font size.
 * It also supports a custom link color. If the color is defined then it will be used as the normal text color.
 *
  */
public class KiteLinkLabel<T> extends LinkLabel<T> {
    private UIUtil.ComponentStyle componentStyle;
    private Color linkColor;

    public KiteLinkLabel(String text, @Nullable Icon icon, @Nullable LinkListener<T> aListener, @Nullable T aLinkData, UIUtil.ComponentStyle componentStyle) {
        super(text, icon, aListener, aLinkData);

        setComponentStyle(componentStyle);
        setIconTextGap(10);
    }

    public Color getLinkColor() {
        return linkColor;
    }

    public void setLinkColor(Color linkColor) {
        this.linkColor = linkColor;
    }

    public final UIUtil.ComponentStyle getComponentStyle() {
        return componentStyle;
    }

    public final void setComponentStyle(@NotNull UIUtil.ComponentStyle componentStyle) {
        this.componentStyle = componentStyle;
        UIUtil.applyStyle(componentStyle, this);
    }

    /**
     * This method adds support for the horizontal text alignment LEFT and LEADING
     *
     * @return
     */
    @NotNull
    protected Rectangle getTextBounds() {
        //todo push this patch into the JetBrains repo upstream
        if (getHorizontalTextPosition() == LEADING || getHorizontalTextPosition() == LEFT) {
            Insets insets = getInsets();

            Dimension size = getPreferredSize();
            Point point = new Point(0, 0);

            Icon icon = getIcon();
            if (icon != null) {
                size.width -= getIconTextGap();
                size.width -= icon.getIconWidth();
            }
            point.y += insets.top;
            size.height -= insets.bottom;

            return new Rectangle(point, size);
        }

        //fallback to the default implementation for all other alignments
        return super.getTextBounds();
    }

    @Override
    protected Color getNormal() {
        return linkColor != null ? linkColor : super.getNormal();
    }

    @Override
    protected void paintComponent(Graphics g) {
        GraphicsUtil.setupAntialiasing(g);
        GraphicsUtil.setupRoundedBorderAntialiasing(g);
        super.paintComponent(g);
    }
}

package com.kite.intellij.lang.documentation;

import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class RenderStyle {
    @Nullable
    private final Color foreground;
    @Nullable
    private final Color background;
    private final boolean boldFont;
    private final boolean italicFont;
    private final RenderStyle fallback;

    public RenderStyle(@Nullable Color foreground, @Nullable Color background, boolean boldFont, boolean italicFont, RenderStyle fallback) {
        this.foreground = foreground;
        this.background = background;
        this.boldFont = boldFont;
        this.italicFont = italicFont;
        this.fallback = fallback;
    }

    public boolean isValid() {
        return getForeground() != null || getBackground() != null || boldFont || italicFont;
    }

    @Nullable
    public Color getForeground() {
        return foreground != null ? foreground : (fallback != null ? fallback.getForeground() : null);
    }

    @Nullable
    public Color getBackground() {
        return background != null ? background : (fallback != null ? fallback.getBackground() : null);
    }

    public boolean isBoldFont() {
        return boldFont;
    }

    public boolean isItalicFont() {
        return italicFont;
    }

    @Override
    public String toString() {
        return "RenderStyle{" +
                "foreground=" + foreground +
                ", background=" + background +
                ", boldFont=" + boldFont +
                ", italicFont=" + italicFont +
                ", fallback=" + fallback +
                '}';
    }
}

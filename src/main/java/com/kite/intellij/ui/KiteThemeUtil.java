package com.kite.intellij.ui;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Access to the current's theme settings.
 * A text editor scheme may be set independently from the general look&feel.
 * IntelliJ always uses the look&feel's color for popups shown in editors, we would like to look similair to the editor's colors.
 * For yet unknown reasons PyCharm 2017.1 paints backgrounds of popups white even if Darcula is configured.
 * <p>
 * This class takes care to display the right colors even if the editor's color schema doesn't fit to the application wide
 * look&feel.
 *
  */
public class KiteThemeUtil {
    private KiteThemeUtil() {
    }

    /**
     * @param scheme The scheme to check.
     * @return true if the given theme is using dark background colors.
     */
    public static boolean isDarkTheme(EditorColorsScheme scheme) {
        return ColorUtil.isDark(scheme.getDefaultBackground());
    }

    /**
     * @param scheme The color scheme to check
     * @return true if the editor scheme fits to the colors of the current look&feel.
     */
    public static boolean isConsistentToLookAndFeel(EditorColorsScheme scheme) {
        return UIUtil.isUnderDarcula() == ColorUtil.isDark(scheme.getDefaultBackground());
    }

    /**
     * @param colorScheme the color scheme to look at
     * @return The background color to use for popups or kite panels displayed in an editor component.
     */
    public static Color getPanelBackground(EditorColorsScheme colorScheme) {
        if (isConsistentToLookAndFeel(colorScheme)) {
            //the default light look&feel has a yellow bg color in 161.x, at least
            return JBColor.lazy(() -> UIManager.getColor("Panel.background"));
        }

        //the theme doesn't fit to the look&feel, i.e. probably a light theme for a dark look&feel (or a dark theme for a light look&feel)
        //the editor theme doesn't fit into to the look&feel, in that case we return our own colors
        //we can't return the editor's background color because the contrast to a popup of the same color would be very low
        return UIUtil.isUnderDarcula() ? JBColor.WHITE : colorScheme.getDefaultBackground().brighter();
    }

    public static Color getDocPanelBackground() {
        return getDocPanelBackground(EditorColorsManager.getInstance().getGlobalScheme());
    }

    public static Color getDocPanelBackground(EditorColorsScheme scheme) {
        return KiteThemeUtil.isDarkTheme(scheme)
                ? ColorUtil.darker(scheme.getDefaultBackground(), 3)
                : getPanelBackground(scheme);
    }
}

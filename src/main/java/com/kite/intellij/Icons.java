package com.kite.intellij;

import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.PlatformIcons;

import javax.swing.*;

/**
 * List of icons available in the plugin.
 */
public interface Icons {
    Icon KiteSmall = IconLoader.getIcon("/icons/logo.png", Icons.class); // 16x16

    Icon KiteSmallError = IconLoader.getIcon("/icons/kite_small_error.svg", Icons.class); // 16x16

    Icon KiteSmallSync = IconLoader.getIcon("/icons/kite_small_disabled_sync.png", Icons.class); // 16x16

    Icon KiteSmallDisabled = IconLoader.getIcon("/icons/kite_small_disabled.svg", Icons.class); // 16x16

    Icon KiteTinyDisabled = IconLoader.getIcon("/icons/kite_tiny_disabled.svg", Icons.class); // 12x12

    Icon KiteCompletionOverlay = IconLoader.getIcon("/icons/completion-overlay.svg", Icons.class);

    Icon KiteCompletionFunction = new LayeredIcon(PlatformIcons.FUNCTION_ICON, KiteCompletionOverlay);

    Icon KiteCompletionModule = new LayeredIcon(PlatformIcons.CLASS_ICON, KiteCompletionOverlay);

    Icon KiteCompletionInstance = new LayeredIcon(PlatformIcons.VARIABLE_ICON, KiteCompletionOverlay);

    Icon KiteProCompletion = IconLoader.getIcon("/icons/icon-star.svg", Icons.class);

    // not an overlay, it's used when we don't have something else to display
    Icon KiteCompletionFallback = KiteCompletionOverlay; // 16x16

    // status panel icons
    Icon StatusPanelLogo = IconLoader.getIcon("/icons/kite_status.svg", Icons.class);
    Icon StatusPanelStar = KiteProCompletion;
    Icon StatusPanelGift = IconLoader.getIcon("/icons/gift.svg", Icons.class);
    Icon StatusPanelMagnifyingGlass = IconLoader.getIcon("/icons/magnifying-glass.svg", Icons.class);
    Icon StatusPanelControlDials = IconLoader.getIcon("/icons/control-dials.svg", Icons.class);
    Icon StatusPanelQuestionMark = IconLoader.getIcon("/icons/question-mark.svg", Icons.class);
}

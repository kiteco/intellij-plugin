package com.kite.intellij.status;

import com.intellij.diagnostic.IdeMessagePanel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KiteStatusBarWidgetProvider implements StatusBarWidgetProvider {
    @Override
    public @Nullable StatusBarWidget getWidget(@NotNull Project project) {
        return new KiteStatusBarWidget(project);
    }

    @Override
    public @NotNull String getAnchor() {
        return StatusBar.Anchors.before(IdeMessagePanel.FATAL_ERROR);
    }
}

package com.kite.intellij.status;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class KiteStatusBarWidgetFactory implements StatusBarWidgetFactory {
    @Override
    public @NotNull @NonNls String getId() {
        return "kite.statusbar.widgetfactory";
    }

    @Override
    public @NotNull @NlsContexts.ConfigurableName String getDisplayName() {
        return "Kite";
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new KiteStatusBarWidget(project);
    }
}

package com.kite.intellij.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.kite.intellij.status.KiteStatusBarWidget;
import org.jetbrains.annotations.NotNull;

/**
 * Opens Kite's status bar widget.
 *
  */
public class KiteShowStatusAction extends DumbAwareAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        if (project != null) {
            KiteStatusBarWidget.showCurrentPopup(project);
        }
    }
}

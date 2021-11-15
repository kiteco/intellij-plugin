package com.kite.intellij.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.kite.intellij.settings.KiteConfigurable;

/**
 * Opens Kite's status bar widget.
 *
  */
public class KiteShowSettingsAction extends DumbAwareAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        ShowSettingsUtil.getInstance().showSettingsDialog(e.getProject(), KiteConfigurable.class);
    }
}

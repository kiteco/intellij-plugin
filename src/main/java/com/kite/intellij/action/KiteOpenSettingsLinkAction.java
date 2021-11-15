package com.kite.intellij.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.kite.intellij.backend.WebappLinks;
import com.kite.intellij.util.KiteBrowserUtil;

/**
 * Opens Kite's settings page in the copilot.
 *
  */
public class KiteOpenSettingsLinkAction extends DumbAwareAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        KiteBrowserUtil.browse(WebappLinks.getInstance().settingsPage());
    }
}

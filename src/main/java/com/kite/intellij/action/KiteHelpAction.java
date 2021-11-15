package com.kite.intellij.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.kite.intellij.KiteConstants;
import com.kite.intellij.util.KiteBrowserUtil;

/**
 * Opens the Kite help pages about this plugin in the user's browser.
 *
  */
public class KiteHelpAction extends DumbAwareAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        KiteBrowserUtil.browse(KiteConstants.KITE_HELP_URL);
    }
}

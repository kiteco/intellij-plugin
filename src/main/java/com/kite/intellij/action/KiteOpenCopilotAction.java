package com.kite.intellij.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import com.kite.intellij.util.KiteBrowserUtil;

/**
 * Opens the Kite copilot.
 *
  */
public class KiteOpenCopilotAction extends DumbAwareAction {
    private static final Logger LOG = Logger.getInstance("#kite.action");

    @Override
    public void actionPerformed(AnActionEvent e) {
        KiteBrowserUtil.browse("kite://home");
    }
}

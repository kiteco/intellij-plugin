package com.kite.intellij.codeFinder;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.vfs.VirtualFile;
import com.kite.intellij.Icons;
import org.jetbrains.annotations.NotNull;

public class KiteFindRelatedFromFileAction extends AnAction implements DumbAware {
    public KiteFindRelatedFromFileAction() {
        super(Icons.KiteSmall);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
            if (virtualFile == null) {
                return;
            }

            KiteCodeFinderManager.requestRelatedCode(virtualFile, null);
        } catch (KiteFindRelatedError ex) {
            KiteCodeFinderManager.showErrorNotification(e.getProject(), ex.getMessage());
        }
    }
}

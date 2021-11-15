package com.kite.intellij.codeFinder;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.kite.intellij.Icons;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class KiteFindRelatedFromLineAction extends AnAction implements DumbAware {
    public KiteFindRelatedFromLineAction() {
        super(Icons.KiteSmall);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        RequiredData req = getRequired(e);
        if (!req.ok) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        requestRelatedCode(e.getProject(), getRequired(e));
    }

    static void requestRelatedCode(Project project, @Nonnull RequiredData requiredData) {
        try {
            if (!requiredData.ok) {
                return;
            }

            Document doc = requiredData.editor.getDocument();
            int lineNo = doc.getLineNumber(requiredData.caret.getOffset());

            KiteCodeFinderManager.requestRelatedCode(requiredData.virtualFile, lineNo);
        } catch (KiteFindRelatedError ex) {
            KiteCodeFinderManager.showErrorNotification(project, ex.getMessage());
        }
    }

    private RequiredData getRequired(AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        Caret caret = e.getData(CommonDataKeys.CARET);
        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        return new RequiredData(editor, caret, virtualFile);
    }

    static class RequiredData {
        Editor editor;
        Caret caret;
        VirtualFile virtualFile;
        boolean ok;

        RequiredData(Editor editor, Caret caret, VirtualFile file) {
            this.editor = editor;
            this.caret = caret;
            this.virtualFile = file;
            this.ok = editor != null && caret != null && virtualFile != null;
        }
    }
}

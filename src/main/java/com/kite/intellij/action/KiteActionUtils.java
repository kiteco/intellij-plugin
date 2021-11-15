package com.kite.intellij.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;
import com.kite.intellij.lang.KiteLanguageSupport;

import javax.annotation.Nullable;

/**
 * Utility functions to work with actions.
 *
  */
public final class KiteActionUtils {
    private KiteActionUtils() {
    }

    @Nullable
    static PsiFile findCurrentFile(AnActionEvent e) {
        Editor editor = findCurrentEditor(e);
        if (editor == null) {
            return null;
        }

        Project project = editor.getProject();
        if (project == null){
            return null;
        }
        return PsiUtilBase.getPsiFileInEditor(editor, project);
    }

    @Nullable
    private static Editor findCurrentEditor(AnActionEvent e) {
        if (e.getProject() == null) {
            return null;
        }

        return CommonDataKeys.EDITOR.getData(e.getDataContext());
    }

    /**
     * Override this method if you want to customize the condition when the kite action is used.
     *
     * @param e The action event which contains the current context of the action invocation
     * @return {@code true} if the kite action should be used instead of the fallback action
     */
    public static boolean isSupported(AnActionEvent e) {
        PsiFile currentFile = findCurrentFile(e);
        return KiteLanguageSupport.isSupported(currentFile, KiteLanguageSupport.Feature.SignatureInfo);
    }

    public static boolean isSupported(DataContext context) {
        PsiFile currentFile = CommonDataKeys.PSI_FILE.getData(context);
        return KiteLanguageSupport.isSupported(currentFile, KiteLanguageSupport.Feature.SignatureInfo);
    }
}

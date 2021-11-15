package com.kite.intellij.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.psi.PsiFile;
import com.kite.intellij.backend.KiteApiService;
import com.kite.intellij.backend.http.KiteHttpException;
import com.kite.intellij.backend.model.SymbolExt;
import com.kite.intellij.backend.response.HoverResponse;
import com.kite.intellij.editor.util.FileEditorUtil;
import com.kite.intellij.lang.KiteLanguageSupport;
import com.kite.intellij.platform.fs.CanonicalFilePath;
import com.kite.intellij.platform.fs.CanonicalFilePathFactory;
import com.kite.intellij.ui.SwingWorkerUtil;
import com.kite.intellij.util.KiteBrowserUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Displays Kite's documentation for the current caret position in the autosearch sidebar.
 * This opens the sidebar only if there's documentation available for the current position.
 * If there's none, then this action is disabled.
 */
public class KiteDocsAtCaretAction extends DumbAwareAction {
    @Override
    public void update(@NotNull AnActionEvent e) {
        PsiFile file = KiteActionUtils.findCurrentFile(e);
        boolean supported = KiteLanguageSupport.isSupported(file, KiteLanguageSupport.Feature.CopilotDocumentation);
        e.getPresentation().setEnabled(supported);
    }

    /**
     * open kite://docs/{id} to show the docs in the Copilot
     */
    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
        if (editor == null) {
            return;
        }

        CanonicalFilePathFactory canonicalFilePathFactory = CanonicalFilePathFactory.getInstance();
        CanonicalFilePath path = canonicalFilePathFactory.createFor(editor, CanonicalFilePathFactory.Context.Event);
        if (path == null) {
            return;
        }

        String fileContent = FileEditorUtil.contentOf(editor);
        int offset = editor.getCaretModel().getOffset();

        SwingWorkerUtil.compute(
                () -> {
                    try {
                        HoverResponse result = KiteApiService.getInstance().hover(path, fileContent, offset);
                        SymbolExt symbol = result != null ? result.getFirstSymbol() : null;
                        return symbol != null ? symbol.getId() : null;
                    } catch (KiteHttpException e1) {
                        throw new RuntimeException("Retrieving the symbol failed");
                    }
                },
                (symbolId) -> {
                    if (symbolId != null) {
                        // don't escape the path, Copilot doesn't seem to be able to handle escaped values here
                        KiteBrowserUtil.browse("kite://docs/" + symbolId.getValue());
                    }
                }
        );
    }
}

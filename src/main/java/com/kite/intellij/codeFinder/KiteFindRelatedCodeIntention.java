package com.kite.intellij.codeFinder;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.kite.intellij.codeFinder.KiteFindRelatedFromLineAction.RequiredData;
import com.kite.intellij.lang.KiteLanguageSupport;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class KiteFindRelatedCodeIntention implements IntentionAction, DumbAware {
    private String text = "Kite: Find related code";

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getText() {
        return text;
    }

    protected void setText(String text) {
        this.text = text;
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return "Find related code";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        int line = editor.getDocument().getLineNumber(editor.getCaretModel().getOffset());
        boolean supported = KiteLanguageSupport.isSupported(file, KiteLanguageSupport.Feature.CodeFinder)
                && editor.getDocument().getLineStartOffset(line) != editor.getDocument().getLineEndOffset(line);
        if (!supported) {
            return false;
        }

        setText("Kite: Find code in " + project.getName() + " related to this line");
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        RequiredData data = new RequiredData(editor, editor.getCaretModel().getPrimaryCaret(), file.getVirtualFile());
        KiteFindRelatedFromLineAction.requestRelatedCode(project, data);
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}

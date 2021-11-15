package com.kite.intellij.action.signatureInfo;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.kite.intellij.lang.KiteLanguageSupport;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * This typed handler remembers the last typed character for a given editor.
 * <p>
 * IntelliJ triggers the signature auto-popup when `(` was typed. At the time the popup is invoked the new
 * character is not yet inserted into the document. Kite must get the '(' though to make signatures work.
 * <p>
 * IntelliJ invokes the auto-popup manager after "checkAutoPopup()" of typed handlers was invoked.
 * <p>
 * To help our auto-popup manager we remember the last character which was typed in "checkAutoPopup".
 *
  */
public class SignatureTypedHandler extends TypedHandlerDelegate {
    private static final Key<EditorChange> LAST_TYPED_KEY = Key.create("kite.lastTypedChar");
    private static final Logger LOG = Logger.getInstance("#kite.ui.signature");

    /**
     * Returns the character which was last typed in the editor at the current offset. If the caret was moved
     * after the last character was typed, then this will return null.
     *
     * @param editor     Editor to check
     * @param lookBehind If the character is expected to be the one right before the offset (i.e. the caret moved after it was typed)
     * @return The last typed character if there's one for the current offset
     */
    @Nullable
    public static Character getLastTypedCharacter(Editor editor, boolean lookBehind) {
        EditorChange change = LAST_TYPED_KEY.get(editor);
        if (change == null) {
            return null;
        }
        return change.findLastTyped(editor, lookBehind);
    }

    @NotNull
    @Override
    public Result checkAutoPopup(char charTyped, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        // update our last typed character
        if (KiteLanguageSupport.isSupported(file, KiteLanguageSupport.Feature.SignatureInfo)) {
            LAST_TYPED_KEY.set(editor, new EditorChange(charTyped, editor, false));
        }

        return Result.CONTINUE;
    }

    @NotNull
    @Override
    public Result charTyped(char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        if (!KiteLanguageSupport.isSupported(file, KiteLanguageSupport.Feature.SignatureInfo)) {
            return Result.CONTINUE;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("typed: "+ editor.getDocument().getText());
        }
        LAST_TYPED_KEY.set(editor, new EditorChange(c, editor, true));

        if (c == '(' || c == ',' || c == '.') {
            // chars handled by IntelliJ
            return Result.CONTINUE;
        }

        // possibly incomplete call expression, count open parens
        Document document = editor.getDocument();
        int offset = editor.getCaretModel().getOffset();
        String documentText = document.getText();
        if (offset > documentText.length()) {
            return Result.CONTINUE;
        }

        TextRange range = TextRange.create(Math.max(offset - 40, 0), offset); //[0,offset] or [offset-40,offset]
        String text = range.substring(documentText);

        int openParens = text.chars().map(ch -> ch == '(' ? 1 : (ch == ')' ? -1 : 0)).sum();
        if (openParens > 0) {
            AutoPopupController.getInstance(file.getProject()).autoPopupParameterInfo(editor, null);
            return Result.STOP;
        }

        return Result.CONTINUE;
    }

    private static class EditorChange {
        final char typed;
        final long modTimestamp;
        final long offset;

        private EditorChange(char typed, Editor editor, boolean afterTyped) {
            this.typed = typed;
            this.modTimestamp = editor.getDocument().getModificationStamp();
            this.offset = editor.getCaretModel().getOffset() + (afterTyped ? -1 : 0);
        }

        @Nullable
        private Character findLastTyped(Editor editor, boolean lookBehind) {
            Document document = editor.getDocument();
            if (document.getModificationStamp() != modTimestamp) {
                return null;
            }

            int editorOffset = editor.getCaretModel().getOffset();
            if (lookBehind) {
                editorOffset--;
            }
            if (editorOffset != offset) {
                return null;
            }

            return typed;
        }
    }
}

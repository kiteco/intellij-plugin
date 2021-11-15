package com.kite.intellij.editor.completion;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.editorActions.QuoteHandler;
import com.intellij.codeInsight.editorActions.TypedHandler;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.kite.intellij.lang.KiteLanguageSupport;
import org.jetbrains.annotations.NotNull;

/**
 * com.intellij.codeInsight.editorActions.TypedHandler#isAutoPopup doesn't seem to handle insertions of spaces well.
 * It only activates a completion contributor's invokeAutoPopup if a PSIElement was found at the offset where the character was typed.
 * At least in tests there were no PSIElements after a space was typed, a headful instance had PSI element at the offset.
 * This difference is probably caused by the event processing handling in our spec runner test.
 * <p>
 * With this typed handler we enforce code completion after a space in supported files.
 * It follows the logic of com.intellij.codeInsight.editorActions.TypedHandler#execute(com.intellij.openapi.editor.Editor, char, com.intellij.openapi.actionSystem.DataContext)
 * to be sure that we don't break things here.
 *
  */
public class KitePythonAutocompleteTypedHandler extends TypedHandlerDelegate {
    @NotNull
    @Override
    public Result checkAutoPopup(char charTyped, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        if (!KiteLanguageSupport.isSupported(file, KiteLanguageSupport.Feature.BasicSupport)) {
            return Result.CONTINUE;
        }

        // a dot is handled by PyCharm's default implementation
        // only show popup for quotes, if it's likely a closing quote
        // we assume a closing quote for an odd number of quotes on the current line
        // (the typed characters is not yet in the document)
        if (charTyped == '('
                || (charTyped == '"' || charTyped == '\'') && !isClosingQuote(file, editor)) {
            triggerPopup(project, editor);
            // we must not return STOP here because this would disable IntelliJ's
            // handling of auto-inserted parens, braces, etc.
            return Result.CONTINUE;
        }

        // enforce completion after a space
        // this is only done in import statements
        int offset = editor.getCaretModel().getOffset();
        if (charTyped == ' ' && offset >= 6) {
            CharSequence text = editor.getDocument().getCharsSequence();
            while (offset >= 4 && text.charAt(offset - 1) == ' ') {
                offset--;
            }
            if (offset >= 6 && "import".contentEquals(text.subSequence(offset - 6, offset))
                    || offset >= 4 && "from".contentEquals(text.subSequence(offset - 4, offset))) {
                triggerPopup(project, editor);
                return Result.STOP;
            }
        }

        return Result.CONTINUE;
    }

    private void triggerPopup(@NotNull Project project, Editor editor) {
        AutoPopupController.getInstance(project).autoPopupMemberLookup(editor, null);
    }

    /**
     * This is using the IDE's quote handler for the current file.
     *
     * @param file
     * @param editor
     * @return true if a quote typed at the current offset would be a closing quote
     */
    private boolean isClosingQuote(PsiFile file, Editor editor) {
        int offset = editor.getCaretModel().getOffset();
        if (offset >= 1 && editor.getDocument().getCharsSequence().charAt(offset - 1) == '\\') {
            return false;
        }

        final QuoteHandler quoteHandler = TypedHandler.getQuoteHandler(file, editor);
        if (quoteHandler == null) {
            return false;
        }

        HighlighterIterator iterator = ((EditorEx) editor).getHighlighter().createIterator(offset);
        if (iterator.atEnd()) {
            return false;
        }
        return quoteHandler.isClosingQuote(iterator, offset);
    }
}

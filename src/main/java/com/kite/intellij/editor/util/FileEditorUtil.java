package com.kite.intellij.editor.util;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

/**
 */
public class FileEditorUtil {
    public static String contentOf(Editor editor) {
        return contentOf(editor.getDocument());
    }

    public static String contentOf(Document document) {
        return document.getText();
    }

    /**
     * Returns the content of the file. This method must not access a {@link VirtualFile} because
     * {@link VfsUtilCore#loadText(VirtualFile)} loads the file from disk, but {@link PsiFile#getText()} returns the cached content.
     *
     * @param file The file whose content should be returned.
     * @return The content of the given file.
     */
    public static String contentOf(PsiFile file) {
        return file.getText();
    }

    /**
     * @param editor
     * @param range
     * @return {@code true} if the text range spans more than one line (logical position) in the editor
     */
    public static boolean isSpanningMulitpleLines(Editor editor, TextRange range) {
        return editor.offsetToLogicalPosition(range.getStartOffset()).line
                != editor.offsetToLogicalPosition(range.getEndOffset()).line;
    }
}

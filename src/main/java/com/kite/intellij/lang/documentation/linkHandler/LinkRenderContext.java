package com.kite.intellij.lang.documentation.linkHandler;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.kite.intellij.editor.util.FileEditorUtil;
import com.kite.intellij.platform.fs.CanonicalFilePath;
import com.kite.intellij.platform.fs.CanonicalFilePathFactory;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * The context a link occurs in.
 *
  */
@Immutable
public class LinkRenderContext {
    private final Project project;
    private final CanonicalFilePath filePath;
    private final int caretOffset;
    private final String editorContent;

    public LinkRenderContext(Project project, CanonicalFilePath filePath, String editorContent, int caretOffset) {
        this.project = project;
        this.editorContent = editorContent;
        this.filePath = filePath;
        this.caretOffset = caretOffset;
    }

    @TestOnly
    LinkRenderContext(Editor editor, CanonicalFilePath filePath) {
        this.project = editor.getProject();
        this.caretOffset = editor.getCaretModel().getOffset();
        this.editorContent = FileEditorUtil.contentOf(editor);
        this.filePath = filePath;
    }

    @Nullable
    public static LinkRenderContext create(@Nonnull Editor editor) {
        CanonicalFilePathFactory filePathFactory = CanonicalFilePathFactory.getInstance();
        CanonicalFilePath filePath = filePathFactory.createFor(editor, CanonicalFilePathFactory.Context.Event);
        if (filePath == null) {
            return null;
        }

        return new LinkRenderContext(editor.getProject(), filePath, FileEditorUtil.contentOf(editor), editor.getCaretModel().getOffset());
    }

    public int getCaretOffset() {
        return caretOffset;
    }

    public String getEditorContent() {
        return editorContent;
    }

    public Project getProject() {
        return project;
    }

    public CanonicalFilePath getFilePath() {
        return filePath;
    }
}

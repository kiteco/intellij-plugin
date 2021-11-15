package com.kite.intellij.platform.fs;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Creates instances of {@link CanonicalFilePathFactory} for the current OS. There may be different implementations for production
 * and test modes.
 *
  */
public interface CanonicalFilePathFactory {
    @Nonnull
    static CanonicalFilePathFactory getInstance() {
        return ServiceManager.getService(CanonicalFilePathFactory.class);
    }

    @Nullable
    CanonicalFilePath createFor(@Nonnull Editor editor, @Nonnull Context context);

    @Nullable
    CanonicalFilePath createFor(@Nonnull Document document, @Nonnull Context context);

    @Nullable
    CanonicalFilePath createFor(@Nonnull PsiFile file, @Nonnull Context context);

    @Nullable
    CanonicalFilePath createFor(@Nonnull VirtualFile file, @Nonnull Context context);

    @Nullable
    CanonicalFilePath createForSupported(@Nonnull VirtualFile file);

    @Nonnull
    CanonicalFilePath forNativePath(@Nonnull String nativeFilePath);

    /**
     * Event: Files, where kited is able to handle events and completions.
     * For example, local files on disk.
     * <p>
     * CodeCompletion: Files, where code completion is possible, but file events are not handled by kited.
     * For example, virtual or remote files.
     * <p>
     * CodeFinder: Files where FindRelatedCode commands are possible.
     * <p>
     * AnyFile: Any file, even if it's unsupported. Currently, this is only used by the JSON test runner.
     */
    enum Context {
        Event, CodeCompletion, CodeFinder, AnyFile
    }
}

package com.kite.intellij.platform.fs;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.kite.intellij.lang.KiteLanguageSupport;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

/**
 * Factory service for canonical file paths. It's able to listen to file events and to cache the paths, if needed.
 *
  */
public class DefaultCanonicalFilePathFactory implements CanonicalFilePathFactory {
    public DefaultCanonicalFilePathFactory() {
    }

    @Nullable
    @Override
    public CanonicalFilePath createFor(@Nonnull Editor editor, @Nonnull Context context) {
        return createFor(editor.getDocument(), context);
    }

    @Nullable
    @Override
    public CanonicalFilePath createFor(@Nonnull Document document, @NotNull Context context) {
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if (file == null) {
            return null;
        }

        return createFor(file, context);
    }

    @Nullable
    @Override
    public CanonicalFilePath createFor(@Nonnull PsiFile file, @NotNull Context context) {
        return createFor(file.getVirtualFile(), context);
    }

    @Nullable
    @Override
    public CanonicalFilePath createFor(@Nonnull VirtualFile file, @NotNull Context context) {
        if (!isSupportedFile(file, context)) {
            return null;
        }
        return createForSupported(file);
    }

    /**
     * Returns a canonical file path for a file, which was already verified to be supported.
     *
     * @param file The file, it has to be supported for valid results.
     * @return The canonical file path
     */
    @Nullable
    public CanonicalFilePath createForSupported(@NotNull VirtualFile file) {
        // getCanonicalPath is an expensive operation which could be cached, if necessary.
        // If cached then file renames have to be supported.
        String intellijPath = file.getCanonicalPath();
        if (intellijPath == null) {
            return null;
        }

        // transform the filename, if needed
        // for example, file.ipynb -> file.py, to make it work with kited
        return createFor(KiteLanguageSupport.patchFilename(intellijPath));
    }

    @Nonnull
    @Override
    public CanonicalFilePath forNativePath(@Nonnull String nativeFilePath) {
        if (File.separatorChar == '/') {
            return createFor(nativeFilePath);
        }

        return createFor(nativeFilePath.replace(File.separatorChar, '/'));
    }

    private CanonicalFilePath createFor(@NotNull String intellijSlashDelimited) {
        if (SystemInfo.isWindows) {
            return new WindowsCanonicalPath(intellijSlashDelimited);
        }

        return new UnixCanonicalPath(intellijSlashDelimited);
    }

    /**
     * Checks whether the given file is supported or not.
     *
     * @param file    The file to check
     * @param context The context, where the canonical file path is used.
     * @return {@code true} if the file is supported by this plugin and the Kite backend, {@code false otherwise}
     */
    private boolean isSupportedFile(@NotNull VirtualFile file, @NotNull Context context) {
        switch (context) {
            case AnyFile:
                return file.isValid();
            case CodeFinder:
                return file.isValid() && KiteLanguageSupport.isSupported(file, KiteLanguageSupport.Feature.CodeFinder);
            default:
                return file.isValid() && KiteLanguageSupport.isSupported(file, KiteLanguageSupport.Feature.BasicSupport);
        }
    }
}

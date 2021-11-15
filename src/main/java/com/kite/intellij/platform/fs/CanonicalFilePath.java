package com.kite.intellij.platform.fs;

import com.intellij.openapi.vfs.VirtualFile;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * Wraps a file path and offers conversions into different formats.
 *
  */
@Immutable
public interface CanonicalFilePath {
    /**
     * The format returned by IntelliJ's {@link VirtualFile#getCanonicalPath()}, e.g. C:/Users/userName/file.py
     *
     * @return A file path separated by /
     */
    @Nonnull
    String asSlashDelimitedPath();

    /**
     * The format which references the file with the current OS of the user, e.g. C:\Users|userName\file.py on windows or
     * /Users/userName/file.py on Mac (no the same file here, though).
     *
     * @return The path separated by {@link java.io.File#separatorChar}
     */
    @Nonnull
    String asOSDelimitedPath();

    /**
     * The special format used by kite to put path values into urls. On Mac and linux it's / replaced by :
     * On windows there's an additional :windows prefix, e.g. :windows:C:Users:userName:file.py
     *
     * @return The file in the format required by kite.
     */
    @Nonnull
    String asKiteEncodedPath();

    /**
     * Returns the last segment of the file path. This usually is the filename for files or the name of a directory.
     * If no path separator was found then the full path is returned as fallback.
     *
     * @return The last segment of the path.
     */
    @Nonnull
    default String filename() {
        String path = asSlashDelimitedPath();
        int reverseLookupIndex = path.endsWith("/") ? path.length() - 1 : path.length();

        int lastSlash = path.lastIndexOf('/');
        if (lastSlash > 0) {
            return path.substring(lastSlash + 1, reverseLookupIndex);
        }

        return path;
    }

    @Nonnull
    default String filenameExtension() {
        String file = filename();
        int index = file.lastIndexOf('.');
        if (index == -1 || index >= file.length() - 1) {
            return "";
        }

        return file.substring(index + 1).toLowerCase();
    }
}

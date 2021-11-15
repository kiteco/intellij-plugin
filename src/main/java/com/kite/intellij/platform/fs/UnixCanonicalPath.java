package com.kite.intellij.platform.fs;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Default unix path implementation.
 *
  */
public class UnixCanonicalPath implements CanonicalFilePath {
    @Nonnull
    private final String slashPath;

    public UnixCanonicalPath(@Nonnull String slashPath) {
        this.slashPath = slashPath;
    }

    @Nonnull
    @Override
    public String asSlashDelimitedPath() {
        return slashPath;
    }

    @Nonnull
    @Override
    public String asOSDelimitedPath() {
        return slashPath;
    }

    @Nonnull
    @Override
    public String asKiteEncodedPath() {
        return slashPath.replace('/', ':');
    }

    @Override
    public int hashCode() {
        return Objects.hash(slashPath);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UnixCanonicalPath that = (UnixCanonicalPath) o;
        return Objects.equals(slashPath, that.slashPath);
    }

    @Override
    public String toString() {
        return "UnixCanonicalPath{" +
                "slashPath='" + slashPath + '\'' +
                '}';
    }
}

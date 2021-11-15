package com.kite.intellij.platform.fs;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Objects;

/**
 * Windows implementation of canonical paths. Relative paths are supported and will not be prefixed by :windows: .
 *
  */
@Immutable
public class WindowsCanonicalPath implements CanonicalFilePath {
    @Nonnull
    private final String slashPath;

    public WindowsCanonicalPath(@Nonnull String slashPath) {
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
        return slashPath.replace('/', '\\');
    }

    @Nonnull

    @Override
    public String asKiteEncodedPath() {
        if (slashPath.indexOf(':') == -1) {
            //no drive specified, this won't happen in production as only absolute paths are used there
            //we do this for our tests
            return slashPath.replace('/', ':');
        }

        return ":windows:" + slashPath.substring(0, 1) + slashPath.substring(2).replace('/', ':');
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
        WindowsCanonicalPath that = (WindowsCanonicalPath) o;
        return Objects.equals(slashPath, that.slashPath);
    }

    @Override
    public String toString() {
        return "WindowsCanonicalPath{" +
                "slashPath='" + slashPath + '\'' +
                '}';
    }
}

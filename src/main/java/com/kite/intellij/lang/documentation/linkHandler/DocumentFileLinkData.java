package com.kite.intellij.lang.documentation.linkHandler;

import com.kite.intellij.platform.fs.CanonicalFilePath;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Objects;
import java.util.OptionalInt;

@Immutable
@ThreadSafe
public class DocumentFileLinkData implements KiteLinkData {
    private final String file;
    private final OptionalInt line;

    public DocumentFileLinkData(CanonicalFilePath path, OptionalInt line) {
        this(path.asOSDelimitedPath(), line);
    }

    public DocumentFileLinkData(String nativePath, int line) {
        this(nativePath, OptionalInt.of(line));
    }

    public DocumentFileLinkData(String file, OptionalInt line) {
        this.file = file;
        this.line = line;
    }

    public OptionalInt getLine() {
        return line;
    }

    public String getFile() {
        return file;
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, line);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DocumentFileLinkData that = (DocumentFileLinkData) o;
        return Objects.equals(file, that.file) &&
                Objects.equals(line, that.line);
    }

    @Override
    public String toString() {
        return "DocumentFileLinkData{" +
                "file='" + file + '\'' +
                ", line=" + line +
                '}';
    }
}

package com.kite.intellij.backend.model;

import java.util.Objects;

/**
 */
public class Usage {
    public static final Usage[] EMPTY = new Usage[0];

    private final String code;
    private final String filename;
    private final int line;

    public Usage(String code, String filename, int line) {
        this.code = code;
        this.filename = filename;
        this.line = line;
    }

    public String getCode() {
        return code;
    }

    public String getFilename() {
        return filename;
    }

    public int getLine() {
        return line;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, filename, line);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Usage usage = (Usage) o;
        return line == usage.line &&
                Objects.equals(code, usage.code) &&
                Objects.equals(filename, usage.filename);
    }

    @Override
    public String toString() {
        return "Usage{" +
                "code='" + code + '\'' +
                ", filename='" + filename + '\'' +
                ", line=" + line +
                '}';
    }
}

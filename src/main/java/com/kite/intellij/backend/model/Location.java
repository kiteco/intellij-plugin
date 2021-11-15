package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 */
public class Location {
    @Nonnull
    private final String filePath;
    private final int lineNumber;

    public Location(@Nonnull String filePath, int lineNumber) {
        this.filePath = filePath;
        this.lineNumber = lineNumber;
    }

    public static Location of(String filePath, int lineNumber) {
        return new Location(filePath, lineNumber);
    }

    @Nonnull
    public String getFilePath() {
        return filePath;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath, lineNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Location location = (Location) o;
        return lineNumber == location.lineNumber &&
                Objects.equals(filePath, location.filePath);
    }

    @Override
    public String toString() {
        return "Location{" +
                "filePath='" + filePath + '\'' +
                ", lineNumber=" + lineNumber +
                '}';
    }
}

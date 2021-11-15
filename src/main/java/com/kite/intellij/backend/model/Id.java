package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a single ID.
 */
public final class Id implements CharSequence {
    public static final Id EMPTY_ID = new Id("");

    @Nonnull
    private final String value;

    private Id(@Nonnull String value) {
        this.value = value.isEmpty() || value.endsWith(";") ? "" : value;
    }

    public static Id of(@Nullable String id) {
        if (id == null || id.isEmpty() || id.trim().isEmpty()) {
            return EMPTY_ID;
        }

        return new Id(id);
    }

    @Nonnull
    public String getValue() {
        return value;
    }

    @Override
    public int length() {
        return value.length();
    }

    @Override
    public char charAt(int index) {
        return value.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return value.subSequence(start, end);
    }

    public boolean isNotEmpty() {
        return !value.isEmpty();
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Id id = (Id) o;

        return value.equals(id.value);
    }

    @Override
    public String toString() {
        return "Id{" +
                "value='" + value + '\'' +
                '}';
    }
}

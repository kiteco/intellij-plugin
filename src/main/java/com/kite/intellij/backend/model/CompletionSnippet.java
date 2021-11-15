package com.kite.intellij.backend.model;

import java.util.Arrays;
import java.util.Objects;

public class CompletionSnippet {
    private final String text;
    private final CompletionRange[] placeholders;

    public CompletionSnippet(String text, CompletionRange[] placeholders) {
        this.text = text;
        this.placeholders = placeholders;
    }

    public static CompletionSnippet of(String value){
        return new CompletionSnippet(value, CompletionRange.EMPTY_ARRAY);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(text);
        result = 31 * result + Arrays.hashCode(placeholders);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CompletionSnippet that = (CompletionSnippet) o;
        return Objects.equals(text, that.text) &&
                Arrays.equals(placeholders, that.placeholders);
    }

    @Override
    public String toString() {
        return "CompletionSnippet{" +
                "text='" + text + '\'' +
                ", placeholders=" + Arrays.toString(placeholders) +
                '}';
    }

    public String getText() {
        return text;
    }

    public CompletionRange[] getPlaceholders() {
        return placeholders;
    }
}

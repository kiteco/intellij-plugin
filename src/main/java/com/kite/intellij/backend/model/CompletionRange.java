package com.kite.intellij.backend.model;

import java.util.Objects;

public class CompletionRange {
    public static CompletionRange[] EMPTY_ARRAY = new CompletionRange[0];

    private final int begin;
    private final int end;

    public CompletionRange(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(begin, end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CompletionRange that = (CompletionRange) o;
        return begin == that.begin &&
                end == that.end;
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "CompletionRange2{" +
                "begin=" + begin +
                ", end=" + end +
                '}';
    }
}

package com.kite.intellij.backend.model;

/**
 */
public class TextSelection {
    private final int startOffset;
    private final int endOffset;

    private TextSelection(int startOffset, int endOffset) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    private TextSelection(int offset) {
        this.startOffset = offset;
        this.endOffset = offset;
    }

    public static TextSelection create(int startOffset, int endOffset) {
        return new TextSelection(startOffset, endOffset);
    }

    public static TextSelection create(int offset) {
        return new TextSelection(offset);
    }

    @Override
    public int hashCode() {
        int result = startOffset;
        result = 31 * result + endOffset;
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

        TextSelection that = (TextSelection) o;

        return startOffset == that.startOffset && endOffset == that.endOffset;
    }

    @Override
    public String toString() {
        return String.format("[%d,%d]", startOffset, endOffset);
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }
}

package com.kite.intellij.backend.response;

import com.google.common.collect.Iterators;
import com.kite.intellij.backend.model.KiteCompletion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

/**
 * Defines the response to a completion request. It contains a list of suggestions.
 * <p>
 * This class is immutable.
 *
  */
public class KiteCompletions implements Iterable<KiteCompletion> {
    public static final KiteCompletions EMPTY = new KiteCompletions(0, 0, KiteCompletion.EMPTY);

    private final long begin;
    private final long end;
    private final KiteCompletion[] suggestions;

    public KiteCompletions(long begin, long end, @Nullable KiteCompletion[] suggestions) {
        this.begin = begin;
        this.end = end;
        this.suggestions = ((suggestions == null) || (suggestions.length == 0)) ? KiteCompletion.EMPTY : suggestions;
    }

    public long getBegin() {
        return begin;
    }

    public long getEnd() {
        return end;
    }

    public KiteCompletion[] getItems() {
        return suggestions;
    }

    public int size() {
        return suggestions.length;
    }

    public boolean isEmpty() {
        return suggestions.length == 0;
    }

    @Nonnull
    @Override
    public Iterator<KiteCompletion> iterator() {
        return Iterators.forArray(suggestions);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(begin, end);
        result = 31 * result + Arrays.hashCode(suggestions);
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
        KiteCompletions that = (KiteCompletions) o;
        return begin == that.begin &&
                end == that.end &&
                Arrays.equals(suggestions, that.suggestions);
    }

    @Override
    public String toString() {
        return "CompletionResponse{" +
                "begin=" + begin +
                ", end=" + end +
                ", suggestions=" + Arrays.toString(suggestions) +
                '}';
    }
}

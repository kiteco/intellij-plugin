package com.kite.intellij.backend.model;

import com.google.common.collect.Iterators;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

/**
 * {
 * "calls": [CALL, CALL, ...]
 * }
 */
@Immutable
@ThreadSafe
public class Calls implements Iterable<Call> {
    private final Call[] calls;

    public Calls(Call[] calls) {
        this.calls = calls == null || calls.length == 0 ? Call.EMPTY_ARRAY : calls;
    }

    @Override
    public int hashCode() {
        return Objects.hash((Object[]) calls);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Calls calls1 = (Calls) o;
        return Arrays.equals(calls, calls1.calls);
    }

    @Override
    public String toString() {
        return "Calls{" +
                "calls=" + Arrays.toString(calls) +
                '}';
    }

    public Call[] getCalls() {
        return calls;
    }

    public boolean isEmpty() {
        return calls.length == 0;
    }

    public int size() {
        return calls.length;
    }

    @Nonnull
    @Override
    public Iterator<Call> iterator() {
        return Iterators.forArray(calls);
    }

    @Nullable
    public Call getFirstCall() {
        return isEmpty() ? null : calls[0];
    }
}

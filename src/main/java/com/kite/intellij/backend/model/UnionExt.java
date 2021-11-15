package com.kite.intellij.backend.model;

import com.google.common.collect.Iterators;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

/**
 */
@Immutable
@ThreadSafe
public class UnionExt implements Iterable<ValueExt> {
    @Nonnull
    private final ValueExt[] values;

    public UnionExt(ValueExt... values) {
        this.values = values == null || values.length == 0 ? ValueExt.EMPTY_ARRAY : values;
    }

    @Nonnull
    @Override
    public Iterator<ValueExt> iterator() {
        return Iterators.forArray(values);
    }

    @Nonnull
    public ValueExt[] getValues() {
        return values;
    }

    public boolean isEmpty() {
        return values.length == 0;
    }

    public int size() {
        return values.length;
    }

    @Override
    public int hashCode() {
        return Objects.hash((Object[]) values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UnionExt valueExts = (UnionExt) o;
        return Arrays.equals(values, valueExts.values);
    }

    @Override
    public String toString() {
        return "UnionExt{" +
                "values=" + Arrays.toString(values) +
                '}';
    }
}

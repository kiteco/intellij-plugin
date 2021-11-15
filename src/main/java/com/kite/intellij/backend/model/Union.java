package com.kite.intellij.backend.model;

import com.google.common.collect.Iterators;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

/**
 * A list of possible values that a symbol might have.
 * [VALUE, VALUE, ...]
 *
  */
@SuppressWarnings("unused")
@Immutable
public class Union implements Iterable<Value> {
    @Nonnull
    private final Value[] values;
    @Nonnull
    private final String[] types;
    @Nonnull
    private final String[] typeIds;

    public Union(Value... values) {
        this.values = values == null || values.length == 0 ? Value.EMPTY : values;

        types = new String[this.values.length];
        typeIds = new String[this.values.length];

        for (int i = 0; i < this.values.length; i++) {
            Value value = this.values[i];
            if (value != null) {
                types[i] = value.getType();
                typeIds[i] = value.getTypeId();
            }
        }
    }

    @Nonnull
    public Value[] getValues() {
        return values;
    }

    @Nonnull
    public String[] getTypes() {
        return types;
    }

    @Nonnull
    public String[] getTypeIds() {
        return typeIds;
    }

    public int size() {
        return values.length;
    }

    public boolean isEmpty() {
        return values.length == 0;
    }

    @Nullable
    public Value getFirst() {
        return isEmpty() ? null : values[0];
    }

    @Nonnull
    @Override
    public Iterator<Value> iterator() {
        return Iterators.forArray(values);
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
        Union union = (Union) o;
        return Arrays.equals(values, union.values);
    }

    @Override
    public String toString() {
        return "Union{" +
                "values=" + Arrays.toString(values) +
                '}';
    }
}

package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Arrays;
import java.util.Objects;

/**
 * {
 * bases: [BASE, BASE, ...]
 * module: STRING    // fully qualified module name
 * module_id: ID
 * members: [SYMBOL, SYMBOL, ...]  // empty except for modules and classes
 * }
 */
@Immutable
@ThreadSafe
public class PythonTypeDetails extends ForwardingTypeDetails {
    @Nonnull
    private final Base[] bases;
    private final PythonFunctionDetails constructor;

    public PythonTypeDetails(@Nonnull TypeDetailsBase base, @Nullable Base[] bases, PythonFunctionDetails constructor) {
        super(base);

        this.bases = bases == null || bases.length == 0 ? Base.EMPTY_ARRAY : bases;
        this.constructor = constructor;
    }

    public PythonFunctionDetails getConstructor() {
        return constructor;
    }

    public boolean hasConstructor() {
        return constructor != null;
    }

    public boolean hasBases() {
        return bases.length > 0;
    }

    @Nonnull
    public Base[] getBases() {
        return bases;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bases, base, constructor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PythonTypeDetails that = (PythonTypeDetails) o;
        return Objects.equals(base, that.base)
                && Objects.equals(constructor, that.constructor)
                && Arrays.equals(bases, that.bases);
    }

    @Override
    public String toString() {
        return "PythonTypeDetails{" +
                "base=" + base +
                "constructor=" + constructor +
                ", bases=" + Arrays.toString(bases) +
                '}';
    }
}

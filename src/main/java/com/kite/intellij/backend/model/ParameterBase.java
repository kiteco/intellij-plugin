package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Objects;

/**
 * {
 * name: STRING
 * default_value: UNION  // explicit default value
 * inferred_value: UNION // values inferred by type inference
 * annotation: UNION     // explicit type annotation
 * keyword_only: BOOL    // true if this is a keyword-only parameter
 * synopsis: STRING      // short documentation string
 * }
 */
@ThreadSafe
@Immutable
public class ParameterBase implements Parameter {
    @Nonnull
    private final String name;
    private final String synopsis;
    @Nullable
    private final Union inferredValue;

    public ParameterBase(@Nonnull String name, @Nullable Union inferredValue, String synopsis) {
        this.name = name;
        this.inferredValue = inferredValue;
        this.synopsis = synopsis;
    }

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    @Nullable
    public Union getInferredValue() {
        return inferredValue;
    }

    @Override
    public boolean hasInferredValue() {
        return inferredValue != null;
    }

    @Override
    public String getSynopsis() {
        return synopsis;
    }

    @Override
    public boolean isSynopsisAvailable() {
        return synopsis != null;
    }

    /**
     * Returns the type which could be displayed. First it looks at the explicit type annotation, then at the
     * inferred type and then at the default value
     *
     * @return The type to display, if available
     */
    @Override
    @Nullable
    public Union getDisplayedValues() {
        if (inferredValue != null && !inferredValue.isEmpty()) {
            return inferredValue;
        }

        return null;
    }

    /**
     * Returns the type which could be displayed. First it looks at the explicit type annotation, then at the
     * inferred type and then at the default value
     *
     * @return The type to display, if available
     */
    @Override
    @Nullable
    public String[] getDisplayedTypes() {
        Union displayedValues = getDisplayedValues();
        return displayedValues != null ? displayedValues.getTypes() : null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, inferredValue, synopsis);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ParameterBase parameter = (ParameterBase) o;
        return Objects.equals(name, parameter.name) &&
                Objects.equals(inferredValue, parameter.inferredValue) &&
                Objects.equals(synopsis, parameter.synopsis);
    }

    @Override
    public String toString() {
        return "Parameter{" +
                "name='" + name + '\'' +
                ", inferredValue=" + inferredValue +
                ", synopsis='" + synopsis + '\'' +
                '}';
    }
}

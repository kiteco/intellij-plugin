package com.kite.intellij.backend.model;

import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * {
 * "id": STRING
 * "name": STRING
 * "examples": [STRING, STRING, ...]
 * }
 */
@Immutable
@ThreadSafe
public class ParameterTypeExample {
    public static final ParameterTypeExample[] EMPTY_ARRAY = new ParameterTypeExample[0];

    @Nonnull
    private final Id id;
    private final String name;
    @Nonnull
    private final List<String> examples;

    public ParameterTypeExample(@Nonnull Id id, String name, @Nullable String[] examples) {
        this.id = id;
        this.name = name;
        this.examples = examples == null ? Collections.emptyList() : Lists.newArrayList(examples);
    }

    @Nonnull
    public Id getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Nonnull
    public List<String> getExamples() {
        return examples;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, examples);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ParameterTypeExample that = (ParameterTypeExample) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(examples, that.examples);
    }

    @Override
    public String toString() {
        return "ParameterTypeExample{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", examples=" + examples +
                '}';
    }
}

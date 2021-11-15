package com.kite.intellij.backend.model;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * {
 * "name": STRING
 * "types": [PARAMETER_TYPE_EXAMPLE, PARAMETER_TYPE_EXAMPLE, ...]
 * }
 */
@Immutable
@ThreadSafe
public class ParameterExample {
    public static final ParameterExample[] EMPTY_ARRAY = new ParameterExample[0];

    private final String name;
    private final ParameterTypeExample[] types;
    private final boolean isKwarg;

    public ParameterExample(String name, boolean isKwarg, ParameterTypeExample[] types) {
        this.name = name;
        this.isKwarg = isKwarg;
        this.types = types == null || types.length == 0 ? ParameterTypeExample.EMPTY_ARRAY : types;
    }

    public String getName() {
        return name;
    }

    public ParameterTypeExample[] getTypes() {
        return types;
    }

    @SuppressWarnings("unused")
    public boolean hasTypes() {
        return types.length > 0;
    }

    @SuppressWarnings("unused")
    public String[] getTypeNames() {
        LinkedHashSet<String> names = new LinkedHashSet<>();

        for (ParameterTypeExample type : types) {
            names.add(type.getName());
        }

        return names.toArray(new String[0]);
    }

    @Nullable
    public String getFirstExample() {
        if (types.length == 0) {
            return null;
        }

        List<String> examples = types[0].getExamples();
        return examples.size() == 0 ? null : examples.get(0);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, types, isKwarg);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ParameterExample that = (ParameterExample) o;
        return Objects.equals(name, that.name) && Objects.equals(isKwarg, that.isKwarg) && Arrays.equals(types, that.types);
    }

    @Override
    public String toString() {
        return "ParameterExample{" +
                "name='" + name + '\'' +
                ", types=" + Arrays.toString(types) +
                ", isKwarg=" + isKwarg +
                '}';
    }

    public boolean isKwarg() {
        return isKwarg;
    }

    public ParameterExample asKwarg() {
        if (isKwarg) {
            return this;
        }

        return new ParameterExample(name, true, types);
    }
}

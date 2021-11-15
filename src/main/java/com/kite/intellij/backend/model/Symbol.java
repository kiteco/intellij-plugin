package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * {
 * id: ID   // identifies the symbol, not the value. Can be empty.
 * name: STRING         // e.g. "name"
 * qualname: STRING     // e.g. "Counter.name"
 * namespace: VALUE
 * value: UNION_EXT
 * synopsis: STRING     // first paragraph from documentation
 * }
 */
public class Symbol implements WithId {
    public static final Symbol[] EMPTY_ARRAY = new Symbol[0];

    @Nonnull
    private final Id id;
    private final String name;
    private final Value namespace;
    @Nullable
    private final Union values;

    public Symbol(@Nullable Id id, String name, Value namespace, @Nullable Union values) {
        this.id = id == null ? Id.EMPTY_ID : id;
        this.name = name;
        this.namespace = namespace;
        this.values = values;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, namespace, values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Symbol symbol = (Symbol) o;
        return Objects.equals(id, symbol.id) &&
                Objects.equals(name, symbol.name) &&
                Objects.equals(namespace, symbol.namespace) &&
                Objects.equals(values, symbol.values);
    }

    @Override
    public String toString() {
        return "Symbol{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", namespace=" + namespace +
                ", values=" + values +
                '}';
    }

    @Nonnull
    @Override
    public Id getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Value getNamespace() {
        return namespace;
    }

    @Nullable
    public Union getValues() {
        return values;
    }

    public boolean hasValues() {
        return values != null && !values.isEmpty();
    }

    public Value getFirstValue() {
        return values != null && hasValues() ? values.iterator().next() : null;
    }
}

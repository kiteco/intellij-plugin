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
public class SymbolExt implements WithId {
    public static final SymbolExt[] EMPTY_ARRAY = new SymbolExt[0];

    @Nonnull
    private final Id id;
    private final String name;
    private final Value namespace;
    private final String qualifiedName;
    @Nullable
    private final UnionExt values;
    private final String synopsis;

    public SymbolExt(@Nullable Id id, String name, String qualifiedName, Value namespace, @Nullable UnionExt values, String synopsis) {
        this.id = id == null ? Id.EMPTY_ID : id;
        this.name = name;
        this.qualifiedName = qualifiedName;
        this.namespace = namespace;
        this.values = values;
        this.synopsis = synopsis;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, namespace, qualifiedName, values, synopsis);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SymbolExt symbolExt = (SymbolExt) o;
        return Objects.equals(id, symbolExt.id) &&
                Objects.equals(name, symbolExt.name) &&
                Objects.equals(namespace, symbolExt.namespace) &&
                Objects.equals(qualifiedName, symbolExt.qualifiedName) &&
                Objects.equals(values, symbolExt.values) &&
                Objects.equals(synopsis, symbolExt.synopsis);
    }

    @Override
    public String toString() {
        return "SymbolExt{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", namespace=" + namespace +
                ", qualifiedName='" + qualifiedName + '\'' +
                ", value=" + values +
                ", synopsis='" + synopsis + '\'' +
                '}';
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public String getSynopsis() {
        return synopsis;
    }

    @Nullable
    public UnionExt getValues() {
        return values;
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

    public boolean hasValues() {
        return values != null && !values.isEmpty();
    }

    @Nullable
    public ValueExt getFirstValue() {
        return hasValues() && values != null ? values.iterator().next() : null;
    }
}

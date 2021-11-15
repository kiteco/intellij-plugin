package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

/**
 * {
 * id: ID            // can be empty if not identifiable
 * kind: KIND
 * repr: STRING      // e.g. "list", "Model", "numpy", "123"
 * type: STRING
 * type_id: ID
 * components: [VALUE, ...],  // e.g. for "list<tuple<int, str>>"
 * }
 */
public class Value implements WithId {
    public static final Value[] EMPTY = new Value[0];

    @Nonnull
    private final Id id;
    private final Kind kind;
    private final String representation;
    private final String type;
    private final String typeId;
    @Nonnull
    private final Value[] components;

    public Value(@Nonnull Id id, Kind kind, @Nullable String representation, String type, String typeId, @Nullable Value[] components) {
        this.id = id;
        this.kind = kind;
        this.representation = representation;
        this.type = type;
        this.typeId = typeId;
        this.components = (components == null || components.length == 0) ? EMPTY : components;
    }

    @Nonnull
    @Override
    public Id getId() {
        return id;
    }

    public Kind getKind() {
        return kind;
    }

    public String getRepresentation() {
        return representation;
    }

    public String getType() {
        return type;
    }

    public String getTypeId() {
        return typeId;
    }

    @Nonnull
    public Value[] getComponents() {
        return components;
    }

    public boolean hasComponents() {
        return components.length > 0;
    }

    public boolean isFunction() {
        return Kind.Function.equals(kind);
    }

    public boolean isModule() {
        return Kind.Module.equals(kind);
    }

    public boolean isDescriptor() {
        return Kind.Descriptor.equals(kind);
    }

    public boolean isInstance() {
        return Kind.Instance.equals(kind);
    }

    public boolean isType() {
        return Kind.Type.equals(kind);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, kind, representation, type, typeId, components);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Value value = (Value) o;
        return Objects.equals(id, value.id) &&
                kind == value.kind &&
                Objects.equals(representation, value.representation) &&
                Objects.equals(type, value.type) &&
                Objects.equals(typeId, value.typeId) &&
                Arrays.equals(components, value.components);
    }

    @Override
    public String toString() {
        return "Value{" +
                "id='" + id + '\'' +
                ", kind=" + kind +
                ", representation='" + representation + '\'' +
                ", type='" + type + '\'' +
                ", typeId='" + typeId + '\'' +
                ", components=" + Arrays.toString(components) +
                '}';
    }

}

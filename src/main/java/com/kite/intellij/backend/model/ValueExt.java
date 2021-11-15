package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Arrays;
import java.util.Objects;

/**
 * {
 * ...everything from value...
 * synopsis: STRING  // first paragraph from documentation
 * detail: FUNCTION_DETAILS | TYPE_DETAILS | OBJECT_DETAILS | MODULE_DETAILS
 * breadcrumbs: [VALUE, VALUE, ...]  // e.g. [<numpy>, <linalg>, <svd>]
 * }
 */
@Immutable
public class ValueExt extends Value {
    public static final ValueExt[] EMPTY_ARRAY = new ValueExt[0];

    @Nullable
    private final String synopsis;
    @Nonnull
    private final Value[] breadcrumbs;
    @Nullable
    private final Detail detail;

    public ValueExt(Value value, @Nullable String synopsis, Value[] breadcrumbs, Detail detail) {
        this(value.getId(), value.getKind(), value.getRepresentation(), value.getType(), value.getTypeId(), value.getComponents(), synopsis, breadcrumbs, detail);
    }

    public ValueExt(Id id, Kind kind, String representation, String type, String typeId, Value[] components, @Nullable String synopsis, @Nullable Value[] breadcrumbs, @Nullable Detail detail) {
        super(id, kind, representation, type, typeId, components);

        this.synopsis = synopsis;
        this.breadcrumbs = (breadcrumbs == null || breadcrumbs.length == 0) ? Value.EMPTY : breadcrumbs;
        this.detail = detail;
    }

    @Nullable
    public String getSynopsis() {
        return synopsis;
    }

    @Nonnull
    public Value[] getBreadcrumbs() {
        return breadcrumbs;
    }

    @Nullable
    public Detail getDetail() {
        return detail;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), synopsis, breadcrumbs, detail);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ValueExt valueExt = (ValueExt) o;
        return Objects.equals(synopsis, valueExt.synopsis) &&
                Arrays.equals(breadcrumbs, valueExt.breadcrumbs) &&
                Objects.equals(detail, valueExt.detail);
    }

    @Override
    public String toString() {
        return "ValueExt{" +
                "synopsis='" + synopsis + '\'' +
                ", breadcrumbs=" + Arrays.toString(breadcrumbs) +
                ", detail=" + detail +
                "} " + super.toString();
    }
}

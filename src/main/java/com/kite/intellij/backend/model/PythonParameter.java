package com.kite.intellij.backend.model;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@Immutable
public class PythonParameter extends ForwardingParameter implements Parameter {
    @Nullable
    private final Union defaultValue;
    @Nullable
    private final Union annotation;
    private final boolean keywordOnly;

    public PythonParameter(ParameterBase base, @Nullable Union defaultValue, @Nullable Union annotation, boolean keywordOnly) {
        super(base);
        this.defaultValue = defaultValue;
        this.annotation = annotation;
        this.keywordOnly = keywordOnly;
    }

    @Nullable
    public Union getDefaultValue() {
        return defaultValue;
    }

    public boolean hasDefaultValue() {
        return defaultValue != null;
    }

    @Nullable
    public Union getAnnotation() {
        return annotation;
    }

    public boolean hasAnnotation() {
        return annotation != null;
    }

    public boolean isKeywordOnly() {
        return keywordOnly;
    }

    @Override
    public String toString() {
        return "PythonParameter{" +
                "defaultValue=" + defaultValue +
                ", annotation=" + annotation +
                ", keywordOnly=" + keywordOnly +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PythonParameter that = (PythonParameter) o;

        if (base != null ? !base.equals(that.base) : that.base != null) {
            return false;
        }
        if (keywordOnly != that.keywordOnly) {
            return false;
        }
        if (defaultValue != null ? !defaultValue.equals(that.defaultValue) : that.defaultValue != null) {
            return false;
        }
        return annotation != null ? annotation.equals(that.annotation) : that.annotation == null;
    }

    @Override
    public int hashCode() {
        int result = defaultValue != null ? defaultValue.hashCode() : 0;
        result = 31 * result + (annotation != null ? annotation.hashCode() : 0);
        result = 31 * result + (keywordOnly ? 1 : 0);
        return result;
    }
}

package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class ForwardingParameter implements Parameter {
    protected final ParameterBase base;

    ForwardingParameter(ParameterBase base) {
        this.base = base;
    }

    @Override
    @Nonnull
    public String getName() {
        return base.getName();
    }

    @Override
    @Nullable
    public Union getInferredValue() {
        return base.getInferredValue();
    }

    @Override
    public boolean hasInferredValue() {
        return base.hasInferredValue();
    }

    @Override
    public String getSynopsis() {
        return base.getSynopsis();
    }

    @Override
    public boolean isSynopsisAvailable() {
        return base.isSynopsisAvailable();
    }

    @Override
    @Nullable
    public Union getDisplayedValues() {
        return base.getDisplayedValues();
    }

    @Override
    @Nullable
    public String[] getDisplayedTypes() {
        return base.getDisplayedTypes();
    }

    @Override
    public String toString() {
        return base.toString();
    }
}

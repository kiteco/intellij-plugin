package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A function details implementation which delegates all methods.
 *
  */
@Immutable
@ThreadSafe
abstract class ForwardingFunctionDetails implements FunctionDetails {
    protected final FunctionDetails base;

    public ForwardingFunctionDetails(@Nonnull FunctionDetails base) {
        this.base = base;
    }

    @Override
    public DetailType getType() {
        return base.getType();
    }

    @Override
    @Nonnull
    public Signature[] getSignatures() {
        return base.getSignatures();
    }

    @Override
    @Nonnull
    public Parameter[] getParameters() {
        return base.getParameters();
    }

    @Override
    @Nonnull
    public String[] getParameterNames() {
        return base.getParameterNames();
    }

    @Override
    public boolean isParametersAvailable() {
        return base.isParametersAvailable();
    }

    @Override
    public boolean isAnyParameterAvailable() {
        return base.isAnyParameterAvailable();
    }

    @Override
    @Nullable
    public Union getReturnValue() {
        return base.getReturnValue();
    }

    @Override
    public boolean isReturnValueAvailable() {
        return base.isReturnValueAvailable();
    }

    @Override
    @Nullable
    public Parameter getParameter(int index) {
        return base.getParameter(index);
    }
}

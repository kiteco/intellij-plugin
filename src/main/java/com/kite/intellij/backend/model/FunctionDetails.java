package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

@Immutable
@ThreadSafe
public interface FunctionDetails extends Detail {
    @Nonnull
    Signature[] getSignatures();

    @Nonnull
    Parameter[] getParameters();

    @Nonnull
    String[] getParameterNames();

    /**
     * @return {@code true} if the signature contains any parameter
     */
    boolean isParametersAvailable();

    boolean isAnyParameterAvailable();

    @Nullable
    Union getReturnValue();

    boolean isReturnValueAvailable();

    @Nullable
    Parameter getParameter(int index);
}

package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Parameter {
    Parameter[] EMPTY_ARRAY = new Parameter[0];

    @Nonnull
    String getName();

    @Nullable
    Union getInferredValue();

    boolean hasInferredValue();

    String getSynopsis();

    boolean isSynopsisAvailable();

    @Nullable
    Union getDisplayedValues();

    @Nullable
    String[] getDisplayedTypes();
}

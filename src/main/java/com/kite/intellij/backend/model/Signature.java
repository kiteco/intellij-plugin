package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;

public interface Signature {
    Signature[] EMPTY_ARRAY = new Signature[0];

    @Nonnull
    ParameterExample[] getArgs();

    @SuppressWarnings("unused")
    @Nonnull
    ParameterExample[] getCombinedArgs();
}

package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;

abstract class ForwardingSignature implements Signature {
    protected final SignatureBase base;

    public ForwardingSignature(SignatureBase base) {
        this.base = base;
    }

    @Override
    @Nonnull
    public ParameterExample[] getArgs() {
        return base.getArgs();
    }
}

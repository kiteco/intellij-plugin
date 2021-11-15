package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Arrays;
import java.util.Objects;

/**
 * {
 * "args": [PARAMETER_EXAMPLE, PARAMETER_EXAMPLE, ...]
 * "kwargs": [PARAMETER_EXAMPLE, PARAMETER_EXAMPLE, ...]
 * }
 */
@Immutable
@ThreadSafe
public class SignatureBase implements Signature {

    @Nonnull
    private final ParameterExample[] args;
    @Nonnull
    private final ParameterExample[] combinedArgs;

    public SignatureBase(@Nullable ParameterExample[] args) {
        this.args = args == null || args.length == 0 ? ParameterExample.EMPTY_ARRAY : args;

        this.combinedArgs = this.args;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(args);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SignatureBase signature = (SignatureBase) o;
        return Arrays.equals(args, signature.args);
    }

    @Override
    public String toString() {
        return "Signature{" +
                "args=" + Arrays.toString(args) +
                '}';
    }

    @Override
    @Nonnull
    public ParameterExample[] getArgs() {
        return args;
    }

    @Override
    @SuppressWarnings("unused")
    @Nonnull
    public ParameterExample[] getCombinedArgs() {
        return combinedArgs;
    }
}

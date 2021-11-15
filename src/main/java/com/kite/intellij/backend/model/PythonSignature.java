package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

public class PythonSignature extends ForwardingSignature implements Signature {
    @Nonnull
    private final ParameterExample[] kwargs;
    private ParameterExample[] combinedArgs;

    public PythonSignature(SignatureBase base, @Nullable ParameterExample[] kwargs) {
        super(base);

        this.kwargs = kwargs == null || kwargs.length == 0 ? ParameterExample.EMPTY_ARRAY : kwargs;
        this.combinedArgs = computeCombinedArgs(base.getArgs(), this.kwargs);
    }

    @Nonnull
    public ParameterExample[] getKwargs() {
        return kwargs;
    }

    @Nonnull
    @Override
    public ParameterExample[] getCombinedArgs() {
        return combinedArgs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, kwargs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PythonSignature that = (PythonSignature) o;
        return Objects.equals(base, ((PythonSignature) o).base)
                && Arrays.equals(kwargs, that.kwargs);
    }

    private static ParameterExample[] computeCombinedArgs(@Nonnull ParameterExample[] args, @Nonnull ParameterExample[] kwargs) {
        if (args.length == 0 && kwargs.length == 0) {
            return ParameterExample.EMPTY_ARRAY;
        }

        ParameterExample[] combinedArgs = new ParameterExample[args.length + kwargs.length];
        System.arraycopy(args, 0, combinedArgs, 0, args.length);
        System.arraycopy(kwargs, 0, combinedArgs, args.length, kwargs.length);

        return combinedArgs;
    }
}

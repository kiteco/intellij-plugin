package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

public class FunctionDetailsBase implements FunctionDetails {
    @Nonnull
    protected final Parameter[] parameters;
    @Nullable
    protected final Union returnValue;
    @Nonnull
    protected final Signature[] signatures;
    @Nonnull
    protected final String[] parameterNames;

    public FunctionDetailsBase(Signature[] signatures, @Nullable Parameter[] parameters, @Nullable Union returnValue) {
        this.signatures = signatures == null || signatures.length == 0 ? Signature.EMPTY_ARRAY : signatures;
        this.parameters = parameters == null ? Parameter.EMPTY_ARRAY : parameters;
        this.returnValue = returnValue;

        //computed values
        this.parameterNames = Arrays.stream(this.parameters).map(Parameter::getName).toArray(String[]::new);
    }

    @Override
    @Nonnull
    public Signature[] getSignatures() {
        return signatures;
    }

    @Override
    @Nonnull
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    @Nonnull
    public String[] getParameterNames() {
        return parameterNames;
    }

    @Override
    public boolean isParametersAvailable() {
        return parameters.length > 0;
    }

    @Override
    public boolean isAnyParameterAvailable() {
        return isParametersAvailable();
    }

    @Override
    @Nullable
    public Union getReturnValue() {
        return returnValue;
    }

    @Override
    public boolean isReturnValueAvailable() {
        return returnValue != null;
    }

    /**
     * Returns the parameter at the given index.
     *
     * @param index The parameter at this index will be returned
     * @return Returns the parameter at the given index.
     * @throws IndexOutOfBoundsException If index < 0 or index &gt; size
     */
    @Override
    @Nullable

    public Parameter getParameter(int index) {
        if (index >= 0 && index < parameters.length) {
            return parameters[index];
        }

        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameters, returnValue, signatures, parameterNames);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FunctionDetailsBase that = (FunctionDetailsBase) o;
        return Arrays.equals(parameters, that.parameters) &&
                Objects.equals(returnValue, that.returnValue) &&
                Arrays.equals(signatures, that.signatures) &&
                Arrays.equals(parameterNames, that.parameterNames);
    }

    @Override
    public String toString() {
        return "FunctionDetailsBase{" +
                "parameters=" + Arrays.toString(parameters) +
                ", returnValue=" + returnValue +
                ", signatures=" + Arrays.toString(signatures) +
                ", parameterNames=" + Arrays.toString(parameterNames) +
                '}';
    }

    @Override
    public DetailType getType() {
        return DetailType.Function;
    }
}

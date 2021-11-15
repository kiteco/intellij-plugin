package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Arrays;
import java.util.Objects;

/**
 * {
 * receiver: PARAMETER   // the "self" or "cls" parameter, or omitted
 * parameters: [PARAMETER, PARAMETER, ...]
 * vararg: PARAMETER     // omitted if there is no *args
 * kwarg: PARAMETER      // omitted if there is no **kwargs
 * kwargParameters: [PARAMETER, PARAMETER, ...]  // inferred kwargs
 * return_value: UNION
 * return_annotation: UNION // explicit return annotation
 * class: STRING     // omitted if not a member of a class
 * class_id: ID      // omitted if not a member of a class
 * module: STRING    // fully qualified module name, e.g. "django.db.models"
 * module_id: ID
 * }
 */
@Immutable
@ThreadSafe
public class PythonFunctionDetails extends ForwardingFunctionDetails {
    @Nullable
    private final Parameter receiver;
    @Nullable
    private final Parameter vararg;
    @Nullable
    private final Parameter kwarg;
    @Nonnull
    private final Parameter[] kwargParameters;
    @Nullable
    private final Union returnAnnotation;

    public PythonFunctionDetails(@Nonnull FunctionDetailsBase base, @Nullable Parameter receiver, @Nullable Parameter vararg, @Nullable Parameter kwarg, @Nullable Union returnAnnotation, @Nullable Parameter[] kwargParameters) {
        super(base);

        this.receiver = receiver;
        this.vararg = vararg;
        this.kwarg = kwarg;
        this.kwargParameters = kwargParameters == null || kwargParameters.length == 0 ? Parameter.EMPTY_ARRAY : kwargParameters;
        this.returnAnnotation = returnAnnotation;
    }

    @Nullable
    public Parameter getReceiver() {
        return receiver;
    }

    @Nullable
    public Parameter getVararg() {
        return vararg;
    }

    public boolean hasVararg() {
        return vararg != null;
    }

    @Nullable
    public Parameter getKwarg() {
        return kwarg;
    }

    public boolean hasKwarg() {
        return kwarg != null;
    }

    @Nonnull
    public Parameter[] getKwargParameters() {
        return kwargParameters;
    }

    @Nullable
    public Union getReturnAnnotation() {
        return returnAnnotation;
    }

    public boolean isReturnAnnotationAvailable() {
        return returnAnnotation != null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, receiver, vararg, kwarg, kwargParameters, returnAnnotation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PythonFunctionDetails that = (PythonFunctionDetails) o;
        return Objects.equals(base, that.base) &&
                Objects.equals(receiver, that.receiver) &&
                Objects.equals(vararg, that.vararg) &&
                Objects.equals(kwarg, that.kwarg) &&
                Arrays.equals(kwargParameters, that.kwargParameters) &&
                Objects.equals(returnAnnotation, that.returnAnnotation);
    }

    @Override
    public String toString() {
        return "PythonFunctionDetails{" +
                "receiver=" + receiver +
                ", vararg=" + vararg +
                ", kwarg=" + kwarg +
                ", kwargParameters=" + Arrays.toString(kwargParameters) +
                ", returnAnnotation=" + returnAnnotation +
                '}';
    }

    @Override
    public boolean isAnyParameterAvailable() {
        return isParametersAvailable() || hasVararg() || hasKwarg();
    }
}

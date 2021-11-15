package com.kite.intellij.backend.model;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Arrays;
import java.util.Objects;

/**
 * {
 * "func_name": STRING
 * "callee": VALUE_EXT
 * "arg_index": INT
 * "signatures": [SIGNATURE, SIGNATURE, ...]
 * }
 */
@Immutable
@ThreadSafe
public class Call {
    public static final Call[] EMPTY_ARRAY = new Call[0];

    private final String funcName;
    private final ValueExt callee;
    private final int argIndex;
    private final boolean inKwargs;
    private final Signature[] signatures;

    public Call(String funcName, ValueExt callee, int argIndex, Signature[] signatures, boolean inKwargs) {
        this.funcName = funcName;
        this.callee = callee;
        this.argIndex = argIndex;
        this.signatures = signatures == null || signatures.length == 0 ? Signature.EMPTY_ARRAY : signatures;
        this.inKwargs = inKwargs;
    }

    public boolean isInKwargs() {
        return inKwargs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(funcName, callee, argIndex, inKwargs, signatures);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Call call = (Call) o;
        return Objects.equals(funcName, call.funcName) &&
                argIndex == call.argIndex &&
                inKwargs == call.inKwargs &&
                Objects.equals(callee, call.callee) &&
                Arrays.equals(signatures, call.signatures);
    }

    @Override
    public String toString() {
        return "Call{" +
                "funcName=" + funcName +
                ", callee=" + callee +
                ", argIndex=" + argIndex +
                ", inKwargs=" + inKwargs +
                ", signatures=" + Arrays.toString(signatures) +
                '}';
    }

    public String getFuncName() {
        return funcName;
    }

    public ValueExt getCallee() {
        return callee;
    }

    public int getArgIndex() {
        return argIndex;
    }

    public Signature[] getSignatures() {
        return signatures;
    }
}

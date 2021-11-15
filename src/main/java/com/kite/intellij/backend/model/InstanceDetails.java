package com.kite.intellij.backend.model;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@Immutable
public class InstanceDetails implements Detail {
    @Nullable
    private final Union instanceType;

    public InstanceDetails(@Nullable Union instanceType) {
        this.instanceType = instanceType;
    }

    @Override
    public DetailType getType() {
        return DetailType.Object;
    }

    @Nullable
    public Union getInstanceType() {
        return instanceType;
    }

    @Override
    public int hashCode() {
        return instanceType != null ? instanceType.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InstanceDetails that = (InstanceDetails) o;

        return instanceType != null ? instanceType.equals(that.instanceType) : that.instanceType == null;
    }

    @Override
    public String toString() {
        return "InstanceDetails{" +
                "instanceType=" + instanceType +
                '}';
    }
}

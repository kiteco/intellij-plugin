package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Arrays;
import java.util.Objects;

@Immutable
@ThreadSafe
public class TypeDetailsBase implements TypeDetails {
    protected final int totalMembers;
    @Nonnull
    protected final Symbol[] members;

    public TypeDetailsBase(int totalMembers, @Nullable Symbol[] members) {
        this.totalMembers = totalMembers;
        this.members = members == null || members.length == 0 ? Symbol.EMPTY_ARRAY : members;
    }

    @Override
    @Nonnull
    public Symbol[] getMembers() {
        return members;
    }

    @Override
    public boolean hasMembers() {
        return members.length > 0;
    }

    @Override
    public int getTotalMembers() {
        return totalMembers;
    }

    @Override
    public DetailType getType() {
        return DetailType.Type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalMembers, members);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TypeDetailsBase that = (TypeDetailsBase) o;
        return totalMembers == that.totalMembers &&
                Arrays.equals(members, that.members);
    }

    @Override
    public String toString() {
        return "TypeDetailsBase{" +
                "totalMembers=" + totalMembers +
                ", members=" + Arrays.toString(members) +
                '}';
    }
}

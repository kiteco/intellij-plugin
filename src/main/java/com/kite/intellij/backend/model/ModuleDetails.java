package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

/**
 * {
 * filename: STRING  // path to file, if this is a local module
 * members: [SYMBOL, SYMBOL, ...]  // empty except for modules and classes
 * }
 */
public class ModuleDetails implements Detail {
    @Nullable
    private final String filename;
    private final int totalMembers;
    @Nonnull
    private final Symbol[] members;

    public ModuleDetails(@Nullable String filename, int totalMembers, @Nullable Symbol[] members) {
        this.filename = filename;
        this.totalMembers = totalMembers;

        this.members = members == null || members.length == 0 ? Symbol.EMPTY_ARRAY : members;
    }

    public int getTotalMembers() {
        return totalMembers;
    }

    @Nullable
    public String getFilename() {
        return filename;
    }

    @Nonnull
    public Symbol[] getMembers() {
        return members;
    }

    public boolean hasMembers() {
        return members.length > 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename, totalMembers, members);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModuleDetails that = (ModuleDetails) o;
        return totalMembers == that.totalMembers &&
                Objects.equals(filename, that.filename) &&
                Arrays.equals(members, that.members);
    }

    @Override
    public String toString() {
        return "ModuleDetails{" +
                "filename='" + filename + '\'' +
                ", totalMembers=" + totalMembers +
                ", members=" + Arrays.toString(members) +
                '}';
    }

    @Override
    public DetailType getType() {
        return DetailType.Module;
    }
}

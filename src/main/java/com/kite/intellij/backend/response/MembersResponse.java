package com.kite.intellij.backend.response;

import com.google.common.collect.Iterators;
import com.kite.intellij.backend.model.SymbolExt;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Iterator;

/**
 * The response of a members request. It contains a list of members and the position of the members
 * in the list of all available members.
 *
  */
@Immutable
@ThreadSafe
public class MembersResponse implements Iterable<SymbolExt> {
    private final int total;
    private final int start;
    private final int end;
    private final SymbolExt[] members;

    public MembersResponse(int total, int start, int end, SymbolExt[] members) {
        this.total = total;
        this.start = start;
        this.end = end;

        this.members = members == null || members.length == 0 ? SymbolExt.EMPTY_ARRAY : members;
    }

    public int getTotal() {
        return total;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public SymbolExt[] getMembers() {
        return members;
    }

    @Nonnull
    @Override
    public Iterator<SymbolExt> iterator() {
        return Iterators.forArray(members);
    }

    public boolean isEmpty() {
        return members.length == 0;
    }

    public int size() {
        return members.length;
    }
}

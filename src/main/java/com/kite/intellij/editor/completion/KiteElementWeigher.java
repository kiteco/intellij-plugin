package com.kite.intellij.editor.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementDecorator;
import com.intellij.codeInsight.lookup.LookupElementWeigher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Handles the order of auto completion items in the popup. It orders the items by relevance, i.e.
 * in the order as returned by Kite.
 *
  */
class KiteElementWeigher extends LookupElementWeigher {
    private static final String ID;
    static {
        String id;
        try {
            id = UUID.randomUUID().toString();
        } catch (Exception e) {
            id = "kite.completionWeigher";
        }

        ID = id;
    }

    KiteElementWeigher() {
        super(ID, true, false);
    }

    @Nullable
    @Override
    public Comparable<?> weigh(@Nonnull LookupElement element) {
        LookupElement unwrapped = element;
        while (unwrapped instanceof LookupElementDecorator) {
            LookupElement next = ((LookupElementDecorator<?>) unwrapped).getDelegate();
            if (next == null) {
                break;
            }
            unwrapped = next;
        }
        if (unwrapped instanceof KiteLookupElement) {
            return ((KiteLookupElement) unwrapped).getWeight();
        }
        return null;
    }
}

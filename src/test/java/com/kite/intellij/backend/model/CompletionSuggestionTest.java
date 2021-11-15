package com.kite.intellij.backend.model;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class CompletionSuggestionTest {
    @Test
    public void testBasics() throws Exception {
        KiteCompletion suggestion = new KiteCompletion("display", CompletionSnippet.of("insert"), "hint",
                "html", new CompletionRange(1, 10), false, "webID", null, KiteCompletion.EMPTY);
        Assert.assertEquals("display", suggestion.getDisplay());
        Assert.assertEquals("insert", suggestion.getInsert());
        Assert.assertEquals("hint", suggestion.getHint());

        Assert.assertEquals("Must be equal", suggestion, suggestion);
        Assert.assertNotEquals("Must be the default hashCode", 0, suggestion.hashCode());

        KiteCompletion other = new KiteCompletion("display2", CompletionSnippet.of("insert2"), "hint",
                "html", new CompletionRange(1, 10), false, "webID", null, KiteCompletion.EMPTY);
        Assert.assertNotEquals("Must not be equal", suggestion, other);
        Assert.assertNotEquals("Must not be equal", suggestion.hashCode(), other.hashCode());
    }
}
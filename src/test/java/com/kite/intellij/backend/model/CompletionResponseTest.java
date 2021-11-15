package com.kite.intellij.backend.model;

import com.kite.intellij.backend.response.KiteCompletions;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class CompletionResponseTest {
    @Test
    public void testBasics() {
        KiteCompletion suggestion1 = new KiteCompletion("display", CompletionSnippet.of("insert"),
                "insert", "html", new CompletionRange(1,10), false, "webID", null, KiteCompletion.EMPTY);
        KiteCompletion suggestion2 = new KiteCompletion("display", CompletionSnippet.of("insert"),
                "insert", "html", new CompletionRange(1,10), false, "webID", null, KiteCompletion.EMPTY);

        KiteCompletions response = new KiteCompletions(0, 10, new KiteCompletion[]{suggestion1, suggestion2});
        Assert.assertEquals("Expected the first suggestion", suggestion1, response.getItems()[0]);
        Assert.assertEquals("Expected the first suggestion", suggestion2, response.getItems()[1]);
        Assert.assertFalse("Must not be empty", response.isEmpty());

        Assert.assertEquals(suggestion1, suggestion2);

        KiteCompletions response1 = new KiteCompletions(0, 10, new KiteCompletion[]{suggestion1, suggestion2});
        KiteCompletions response2 = new KiteCompletions(0, 10, new KiteCompletion[]{suggestion1, suggestion2});
        Assert.assertEquals(response1, response2);
    }

    @Test
    public void testIsEmpty() {
        Assert.assertTrue("Response must be empty", new KiteCompletions(0, 10, KiteCompletion.EMPTY).isEmpty());
    }
}
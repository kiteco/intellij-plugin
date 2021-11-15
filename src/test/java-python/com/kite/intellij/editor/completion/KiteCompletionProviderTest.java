package com.kite.intellij.editor.completion;

import com.kite.intellij.backend.MockKiteApiService;
import com.kite.intellij.backend.http.HttpStatusException;
import com.kite.intellij.backend.http.test.MockKiteHttpConnection;
import com.kite.intellij.settings.KiteSettingsService;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class KiteCompletionProviderTest extends KiteLightFixtureTest {
    @Test
    public void testCompletionLoggedOut() {
        KiteSettingsService.getInstance().getState().useNewCompletions = false;

        setupWithErrorStatus("Unauthorized", 401);
        assertNoKiteCompletions();
    }

    @Test
    public void testCompletionNotFound() {
        KiteSettingsService.getInstance().getState().useNewCompletions = false;

        setupWithErrorStatus("Completions not found", 404);
        assertNoKiteCompletions();
    }

    private void assertNoKiteCompletions() {
        MockKiteApiService api = getKiteApiService();

        Assert.assertTrue("No kite elements must be returned", Arrays.stream(myFixture.completeBasic()).noneMatch(KiteLookupElementEx.class::isInstance));

        List<String> history = api.getCallHistoryWithoutCountersAndStatus();
        Assert.assertEquals(1, history.size());
        Assert.assertEquals("Completion request must be send", "completions(/src/test.py, 5 chars, 5)", history.get(0));
    }

    private void setupWithErrorStatus(String httpMessage, int httpStatusCode) {
        MockKiteApiService api = getKiteApiService();
        api.enableHttpCalls();

        myFixture.configureByText("test.py", "json.<caret>");
        MockKiteHttpConnection.getInstance().addPostPathHandler("/clientapi/editor/complete", (path, payload) -> {
            throw new HttpStatusException(httpMessage, httpStatusCode, "");
        });

        // reset events after file was opened. 182.x triggers focus events in tests, earlier versions don't
        MockKiteApiService.getInstance().clearTestData();

    }
}
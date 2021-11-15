package com.kite.intellij.editor.completion;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.project.DumbServiceImpl;
import com.intellij.testFramework.TestDataFile;
import com.kite.intellij.backend.MockKiteApiService;
import com.kite.intellij.backend.http.HttpStatusException;
import com.kite.intellij.backend.http.test.MockKiteHttpConnection;
import com.kite.intellij.http.RequestWithBodyHandler;
import com.kite.intellij.settings.KiteSettings;
import com.kite.intellij.settings.KiteSettingsService;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public class KiteCompletionContributorTest extends KiteLightFixtureTest {
    @Test
    public void testSmartCompletions() {
        Assert.assertFalse(isLexicographicOrder());
        List<String> items = getCompletions("basicCompletions.py", "json/smartCompletion.json", CompletionType.SMART);

        List<String> kiteItems = items.stream().filter(s -> s.startsWith("kite - ")).collect(Collectors.toList());

        Assert.assertEquals("Expected 2 smart kite completion item.", 2, kiteItems.size());
        Assert.assertEquals("The 1st should be format.", "kite - argv", kiteItems.get(0));
        Assert.assertEquals("The 2nd should be format.", "kite - exit()", kiteItems.get(1));
    }

    @Test
    public void testBasicCompletions() {
        Assert.assertFalse(isLexicographicOrder());

        List<String> items = getCompletions("basicCompletions.py", "json/basicCompletion.json", CompletionType.BASIC);

        List<String> kiteItems = items.stream().filter(s -> s.startsWith("kite - ")).collect(Collectors.toList());

        Assert.assertEquals("Expected 19 kite items.", 19, kiteItems.size());
        Assert.assertEquals("The 1st should be format.", "kite - argv", kiteItems.get(0));
        Assert.assertEquals("The last should be istitle", "kite - flags", kiteItems.get(18));
    }

    @Test
    public void testBasicCompletionsLexicographicOrder() {
        Assert.assertFalse(isLexicographicOrder());

        try {
            setLexicographicOrder(true);

            List<String> items = getCompletions("basicCompletions.py", "json/basicCompletion.json", CompletionType.BASIC);

            Assert.assertEquals("Expected 19 items.", 19, items.size());
            Assert.assertEquals("The 1st should be capitalize.", "kite - _getframe()", items.get(0));
            Assert.assertEquals("The last should be zfill", "kite - version_info", items.get(18));
        } finally {
            //default value is false
            setLexicographicOrder(false);
        }
    }

    @Test
    public void testBasicCompletionsDumbMode() {
        try {
            (DumbServiceImpl.getInstance(getProject())).setDumb(true);

            List<String> items = getCompletions("basicCompletions.py", "json/basicCompletion.json", CompletionType.BASIC);
            Assert.assertEquals("Expected 19 items.", 19, items.size());
        } finally {
            (DumbServiceImpl.getInstance(getProject())).setDumb(false);
        }
    }

    @Test
    public void testModuleCompletion() {
        List<String> items = getCompletions("importCompletion.py", "json/importCompletion.json", CompletionType.BASIC);
        Assert.assertEquals("Two kite items expected" + items, 20, items.size());
    }

    @Test
    public void testEmptyCompletionsNoFallback() {
        List<String> items = getCompletions("emptyCompletions.py", "json/emptyCompletion.json", CompletionType.BASIC);
        Assert.assertTrue("If kite returned no completions, then the original PyCharm items must be shown." + items, items.size() >= 1);
    }

    @Test
    public void testFallbackOnError() {
        List<String> items = getCompletions("emptyCompletions.py", (path, payload) -> {
            throw new HttpStatusException("error", 500, "error");
        }, CompletionType.BASIC);
        Assert.assertTrue("If kite returned no completions then the original PyCharm items must be shown." + items, items.size() >= 1);
    }

    @Test
    public void testDisabledAutocompletionSetting() {
        KiteSettings state = KiteSettingsService.getInstance().getState();
        boolean oldValue = state.codeCompletionEnabled;

        state.codeCompletionEnabled = false;
        try {
            List<String> completions = getCompletions("basicCompletions.py", "json/basicCompletion.json", CompletionType.BASIC);
            List<String> kiteItems = completions.stream().filter(s -> s.startsWith("kite - ")).collect(Collectors.toList());

            Assert.assertTrue("No kite completions must be returned if the setting is disabled", kiteItems.isEmpty());
        } finally {
            state.codeCompletionEnabled = oldValue;
        }
    }

    /**
     * Test that Kite's completions are displayed even there's no common prefix or middle-match between
     * current prefix in the editor and Kite's item.
     */
    @Test
    public void testNoTextOverlap(){
        List<String> items = getCompletions("noTextOverlap.py", "json/noTextOverlap.json", CompletionType.BASIC);
        assertEquals(2, items.size());
    }

    /**
     * Test that Kite's items are always at the top, even if there's no prefix match between editor and Kite's item
     */
    @Test
    public void testNoPrefixMatch(){
        List<String> items = getCompletions("noPrefixMatch.py", "json/noPrefixMatch.json", CompletionType.BASIC);

        assertEquals("expected 2 Kite items + 2 IDE items", 4, items.size());
        assertEquals("[]int {}", items.get(0));
        assertEquals("values, []int", items.get(1));
    }

    @Override
    protected String getBasePath() {
        return "python/editor/codeCompletion/";
    }

    private int findCounterEventsCount() {
        return MockKiteApiService.getInstance().getCounterEventHistory().size();
    }

    private List<String> getCompletions(@TestDataFile String filePath, @TestDataFile String jsonCompletionFile, CompletionType completionType) {
        return getCompletions(filePath, (path, payload) -> loadFile(jsonCompletionFile), completionType);
    }

    private List<String> getCompletions(@TestDataFile String filePath, RequestWithBodyHandler onCompletion, CompletionType completionType) {
        MockKiteHttpConnection httpConnection = MockKiteHttpConnection.getInstance();
        httpConnection.addPostPathHandler("/clientapi/editor/complete", onCompletion, getTestRootDisposable());
        httpConnection.addPostPathHandler("/clientapi/editor/event", (path, payload) -> "{}", getTestRootDisposable());

        MockKiteApiService api = MockKiteApiService.getInstance();
        api.enableHttpCalls();

        myFixture.configureByFile(filePath);

        // make sure that we remove events for focus (182.x seems to trigger focus events in tests, earlier versions don't)
        MockKiteApiService.getInstance().clearTestData();

        //after the initial setup to minimize events
        myFixture.complete(completionType);

        return myFixture.getLookupElementStrings();
    }

    // work with different APIs of 161.x and 171.x
    private boolean isLexicographicOrder() {
        UISettings settings = UISettings.getInstance();

        //first first try the old way and fallback to the new method
        try {
            Field property = settings.getClass().getField("SORT_LOOKUP_ELEMENTS_LEXICOGRAPHICALLY");
            Object value = property.get(settings);
            return value instanceof Boolean && Boolean.TRUE.equals(value);
        } catch (Exception e) {
            try {
                Method method = settings.getClass().getMethod("getSortLookupElementsLexicographically");
                Object result = method.invoke(settings);

                return result instanceof Boolean && Boolean.TRUE.equals(result);
            } catch (Exception e1) {
                return false;
            }
        }
    }

    // work with different APIs of 161.x and 171.x
    private void setLexicographicOrder(boolean enabled) {
        UISettings settings = UISettings.getInstance();
        try {
            Field property = settings.getClass().getField("SORT_LOOKUP_ELEMENTS_LEXICOGRAPHICALLY");
            property.setBoolean(settings, enabled);
        } catch (Exception e) {
            try {
                Method method = settings.getClass().getDeclaredMethod("setSortLookupElementsLexicographically", Boolean.TYPE);
                method.invoke(settings, enabled);
            } catch (Exception methodException) {
                throw new IllegalStateException("Unable to change lexicographic ordering", methodException);
            }
        }

    }
}
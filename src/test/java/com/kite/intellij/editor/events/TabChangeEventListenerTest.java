package com.kite.intellij.editor.events;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.kite.intellij.backend.MockKiteApiService;
import com.kite.intellij.test.KiteLightFixtureTest;
import com.kite.intellij.test.KiteTestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Tests the event queue handling and posting.
 *
  */
public class TabChangeEventListenerTest extends KiteLightFixtureTest {
    @Test
    public void testFocusOnTabChangeSupported() throws Exception {
        MockKiteApiService api = MockKiteApiService.getInstance();

        PsiFile first = configureByFileContentAndFocus("a.py", "first");
        PsiFile second = configureByFileContentAndFocus("b.py", "second");
        TestcaseEditorEventListener.sleepForQueueWork(getProject());

        Assert.assertEquals("Expected no focus events: " + api.getCallHistory(), 2, api.getCallHistoryWithoutCountersAndStatus().size());

        AnAction nextTab = ActionManager.getInstance().getAction("NextTab");
        Assert.assertNotNull(nextTab);

        // we need to emulate the next/previous tab action because the actions are disabled in headless mode
        // com.intellij.openapi.fileEditor.impl.TestEditorManagerImpl.openFileImpl3 doesn't trigger a focuc either
        // (a limitation in the headless/test mode)
        switchTab(first);
        switchTab(second);

        TestcaseEditorEventListener.sleepForQueueWork(getProject());
        Assert.assertEquals("Next tab must generate a focus event", 4, api.getCallHistoryWithoutCountersAndStatus().size());
        Assert.assertEquals("Next tab must generate a focus event", "sendEvent(focus, /src/a.py, first, [0,0])", api.getCallHistoryWithoutCountersAndStatus().get(2));
        Assert.assertEquals("Next tab must generate a focus event", "sendEvent(focus, /src/b.py, second, [0,0])", api.getCallHistoryWithoutCountersAndStatus().get(3));
    }

    @Test
    public void testFocusOnTabChangeUnsupported() throws Exception {
        MockKiteApiService api = MockKiteApiService.getInstance();

        configureByFileContentAndFocus("a.txt", "first");
        configureByFileContentAndFocus("b.txt", "second");
        TestcaseEditorEventListener.sleepForQueueWork(getProject());

        Assert.assertEquals("Expected no focus events: " + api.getCallHistoryWithoutCountersAndStatus(), 0, api.getCallHistoryWithoutCountersAndStatus().size());
    }

    @Override
    protected String getBasePath() {
        return "python/editor/events";
    }

    private void switchTab(PsiFile psiFile) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        myFixture.openFileInEditor(psiFile.getVirtualFile());

        try {
            Editor editor = myFixture.getEditor();
            Method method = editor.getClass().getDeclaredMethod("fireFocusGained");
            if (method != null) {
                method.setAccessible(true);
                method.invoke(editor);

                return;
            }
        } catch (NoSuchMethodException ignored) {
            //ignored
        }

        //emulate event as last resort (may happen when we test against an obfuscated (Ultimate) edition)
        KiteTestUtils.emulateFocusEvent(psiFile);
    }
}
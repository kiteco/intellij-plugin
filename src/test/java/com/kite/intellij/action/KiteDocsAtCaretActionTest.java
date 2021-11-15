package com.kite.intellij.action;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jetbrains.python.PythonFileType;
import com.kite.intellij.backend.MockKiteApiService;
import com.kite.intellij.backend.http.test.MockKiteHttpConnection;
import com.kite.intellij.test.KiteLightFixtureTest;
import com.kite.intellij.util.KiteBrowserUtil;

import java.util.List;

public class KiteDocsAtCaretActionTest extends KiteLightFixtureTest {
    public void testAction() {
        MockKiteApiService api = getKiteApiService();
        api.enableHttpCalls();

        MockKiteHttpConnection.getInstance().addGetPathHandler("/api/buffer/intellij/",
                (path, queryParams) -> loadFile("intellij/action/docsAtCaret/hoverResponse.json"), getTestRootDisposable());

        myFixture.configureByText(PythonFileType.INSTANCE, "print<caret>()");

        AnAction action = ActionManager.getInstance().getAction("kite.docsAtCaret");
        assertNotNull("action must be available", action);

        AnActionEvent event = AnActionEvent.createFromAnAction(action, null, "", DataManager.getInstance().getDataContext(myFixture.getEditor().getComponent()));
        action.beforeActionPerformedUpdate(event);
        assertTrue("The action has to be available", event.getPresentation().isEnabled());

        action.actionPerformed(event);

        List<String> history = api.getCallHistoryWithoutCountersAndStatus();
        int size = history.size();
        assertEquals(1, size);
        assertEquals("hover(/src/aaa.py, 7 chars, 5)", history.get(0));

        assertEquals(1, KiteBrowserUtil.openedUrls.size());
        assertEquals("kite://docs/python;requests id", KiteBrowserUtil.openedUrls.get(0));
    }
}
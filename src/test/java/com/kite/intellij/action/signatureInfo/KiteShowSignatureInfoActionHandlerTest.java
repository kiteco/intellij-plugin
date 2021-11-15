package com.kite.intellij.action.signatureInfo;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jetbrains.python.PythonFileType;
import com.kite.intellij.action.KiteActionOverrideLifecycleListener;
import com.kite.intellij.action.MockKiteDelegatingAction;
import com.kite.intellij.backend.MockKiteApiService;
import com.kite.intellij.backend.http.HttpConnectionUnavailableException;
import com.kite.intellij.backend.http.HttpStatusException;
import com.kite.intellij.backend.http.test.MockKiteHttpConnection;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

public class KiteShowSignatureInfoActionHandlerTest extends KiteLightFixtureTest {
    @Test
    public void testFallback() throws Exception {
        MockKiteApiService api = getKiteApiService();
        api.enableHttpCalls();

        MockKiteHttpConnection.getInstance().addPostPathHandler("/clientapi/editor/signatures", (path, payload) -> {
            throw new HttpConnectionUnavailableException("Kite if offline");
        }, getTestRootDisposable());

        myFixture.configureByText(PythonFileType.INSTANCE, "<caret>print()");

        AnAction action = ActionManager.getInstance().getAction(KiteActionOverrideLifecycleListener.INTELLIJ_PARAMETER_INFO_ACTION_ID);
        Assert.assertTrue("Action must be a mock delegating action: " + action, action instanceof MockKiteDelegatingAction);

        AnActionEvent event = AnActionEvent.createFromAnAction(action, null, "", DataManager.getInstance().getDataContext(myFixture.getEditor().getComponent()));
        action.beforeActionPerformedUpdate(event);
        Assert.assertTrue("The action has to be available", event.getPresentation().isEnabled());

        action.actionPerformed(event);
        Assert.assertTrue("The fallback must have been run because Kite is unavailable", ((MockKiteDelegatingAction) action).isFallbackPerformed());
    }

    /**
     * A 404 status indicates that there  is no method call at the current offset.
     */
    @Test
    public void testNoFallbackWith404() throws Exception {
        MockKiteApiService api = getKiteApiService();
        api.enableHttpCalls();
        //404 is the default response if Kite is available

        myFixture.configureByText(PythonFileType.INSTANCE, "<caret>print()");

        AnAction action = ActionManager.getInstance().getAction(KiteActionOverrideLifecycleListener.INTELLIJ_PARAMETER_INFO_ACTION_ID);
        Assert.assertTrue("Action must be a mock delegating action: " + action, action instanceof MockKiteDelegatingAction);

        AnActionEvent event = AnActionEvent.createFromAnAction(action, null, "", DataManager.getInstance().getDataContext(myFixture.getEditor().getComponent()));
        action.beforeActionPerformedUpdate(event);
        Assert.assertTrue("The action has to be available", event.getPresentation().isEnabled());

        action.actionPerformed(event);
        Assert.assertFalse("The fallback must not be executed because Kite returned that there is no method call", ((MockKiteDelegatingAction) action).isFallbackPerformed());
    }

    /**
     * A 403 status indicates that the file is not whitelisted, show a fallback in that case
     */
    @Test
    public void testFallbackWith403() throws Exception {
        MockKiteApiService api = getKiteApiService();
        api.enableHttpCalls();
        //404 is the default response if Kite is available

        MockKiteHttpConnection.getInstance().addPostPathHandler("/clientapi/editor/signatures", (path, payload) -> {
            throw new HttpStatusException("not whitelisted", 403, "");
        }, getTestRootDisposable());

        myFixture.configureByText(PythonFileType.INSTANCE, "<caret>print()");

        AnAction action = ActionManager.getInstance().getAction(KiteActionOverrideLifecycleListener.INTELLIJ_PARAMETER_INFO_ACTION_ID);
        Assert.assertTrue("Action must be a mock delegating action: " + action, action instanceof MockKiteDelegatingAction);

        AnActionEvent event = AnActionEvent.createFromAnAction(action, null, "", DataManager.getInstance().getDataContext(myFixture.getEditor().getComponent()));
        action.beforeActionPerformedUpdate(event);
        Assert.assertTrue("The action has to be available", event.getPresentation().isEnabled());

        action.actionPerformed(event);
        Assert.assertTrue("The fallback must be executed because Kite returned that it's not yet whitelisted", ((MockKiteDelegatingAction) action).isFallbackPerformed());
    }
}
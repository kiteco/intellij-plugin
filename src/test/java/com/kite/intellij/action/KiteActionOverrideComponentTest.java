package com.kite.intellij.action;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

/**
 */
public class KiteActionOverrideComponentTest extends KiteLightFixtureTest {
    @Test
    public void testParamInfoAction() {
        AnAction action = ActionManager.getInstance().getAction(KiteActionOverrideLifecycleListener.INTELLIJ_PARAMETER_INFO_ACTION_ID);

        Assert.assertTrue("The parameter info action must be the kite delegating action, not IntelliJ's built-in action!", action instanceof KiteDelegatingAction);
    }

    @Test
    public void testNextParamAction() {
        AnAction action = ActionManager.getInstance().getAction(KiteActionOverrideLifecycleListener.INTELLIJ_NEXT_PARAMETER_ACTION_ID);

        Assert.assertTrue("The parameter info action must be the kite delegating action, not IntelliJ's built-in action!", action instanceof KiteDelegatingEditorAction);
    }

    @Test
    public void testPrevParamAction() {
        AnAction action = ActionManager.getInstance().getAction(KiteActionOverrideLifecycleListener.INTELLIJ_PREVIOUS_PARAMETER_ACTION_ID);

        Assert.assertTrue("The parameter info action must be the kite delegating action, not IntelliJ's built-in action!", action instanceof KiteDelegatingEditorAction);
    }
}
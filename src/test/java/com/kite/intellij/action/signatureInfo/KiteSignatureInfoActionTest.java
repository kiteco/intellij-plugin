package com.kite.intellij.action.signatureInfo;

import com.intellij.ide.DataManager;
import com.intellij.ide.IdeEventQueue;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.jetbrains.python.PythonFileType;
import com.kite.intellij.action.KiteActionOverrideLifecycleListener;
import com.kite.intellij.action.KiteDelegatingAction;
import com.kite.intellij.backend.MockKiteApiService;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 */
public class KiteSignatureInfoActionTest extends KiteLightFixtureTest {
    @Test
    public void testActionInvocation() {
        myFixture.configureByText(PythonFileType.INSTANCE, "<caret>print()");

        AnAction action = ActionManager.getInstance().getAction(KiteActionOverrideLifecycleListener.INTELLIJ_PARAMETER_INFO_ACTION_ID);
        Assert.assertTrue(action instanceof KiteDelegatingAction);

        AnActionEvent event = AnActionEvent.createFromAnAction(action, null, "", DataManager.getInstance().getDataContext(myFixture.getEditor().getComponent()));
        action.beforeActionPerformedUpdate(event);
        Assert.assertTrue("The action has to be available", event.getPresentation().isEnabled());

        action.actionPerformed(event);

        IdeEventQueue.getInstance().flushQueue();
    }
}
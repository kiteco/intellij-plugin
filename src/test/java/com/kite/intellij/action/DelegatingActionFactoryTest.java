package com.kite.intellij.action;

import com.intellij.codeInsight.hint.actions.NextParameterAction;
import com.intellij.codeInsight.hint.actions.ShowParameterInfoAction;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.kite.intellij.action.signatureInfo.KiteNextParameterAction;
import com.kite.intellij.action.signatureInfo.KiteSignatureInfoAction;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

public class DelegatingActionFactoryTest extends KiteLightFixtureTest {
    @Test
    public void testEditorActionDelegation() throws Exception {
        Assert.assertTrue(DelegatingActionFactory.create(new KiteNextParameterAction(), new NextParameterAction()) instanceof EditorAction);
    }

    @Test
    public void testFallbackDelegation() throws Exception {
        Assert.assertFalse(DelegatingActionFactory.create(new KiteSignatureInfoAction(), new ShowParameterInfoAction()) instanceof EditorAction);
        Assert.assertTrue(DelegatingActionFactory.create(new KiteSignatureInfoAction(), new ShowParameterInfoAction()) instanceof KiteDelegatingAction);
    }
}
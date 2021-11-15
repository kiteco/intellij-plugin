package com.kite.intellij.action;

import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.actions.BackspaceAction;
import com.intellij.openapi.editor.actions.TabAction;
import com.kite.intellij.action.signatureInfo.KiteNextParameterAction;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class KiteParamActionPromoterTest {
    @Test
    public void testNonKitePromotion() throws Exception {
        KiteParamActionPromoter promoter = new KiteParamActionPromoter();
        List<AnAction> promoted = promoter.promote(Lists.newArrayList(new TabAction(), new BackspaceAction()), DataContext.EMPTY_CONTEXT);
        Assert.assertEquals("If no kite action is contained an empty list must be returned", 0, promoted.size());
    }

    @Test
    public void testKiteActionPromotion() throws Exception {
        KiteParamActionPromoter promoter = new KiteParamActionPromoter();
        List<AnAction> promoted = promoter.promote(Lists.newArrayList(new BackspaceAction(), new TabAction(), new KiteNextParameterAction(), new BackspaceAction()), DataContext.EMPTY_CONTEXT);
        Assert.assertEquals("If a kite action is containd the complete list must be reordered", 4, promoted.size());

        Assert.assertTrue("The kite action must be before the tab action", promoted.get(0) instanceof BackspaceAction);
        Assert.assertTrue("The kite action must be before the tab action", promoted.get(1) instanceof KiteNextParameterAction);
        Assert.assertTrue(promoted.get(2) instanceof TabAction);
        Assert.assertTrue(promoted.get(3) instanceof BackspaceAction);
    }
}
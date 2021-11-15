package com.kite.intellij.action;

import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.ActionPromoter;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.actions.TabAction;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * If a "prev/next param" action is in the same set as the "tab" action it has to be ranked higher. Otherwise TAB is inserted
 * and the parameter action is skipped. The order is different compared to IntelliJ because we override the Prev/Next actions
 * which modified the order of the actions.
 *
  */
public class KiteParamActionPromoter implements ActionPromoter {
    /**
     * Returns the list of action which should be promoted. Promoted actions are moved to the start of the list of actions.
     *
     * @param actions The actions which could be executed for the same shortcut
     * @param context The current context
     * @return The actions to promote, an empty list if no actions should be promoted.
     */
    @Override
    public List<AnAction> promote(@NotNull List<? extends AnAction> actions, @NotNull DataContext context) {
        if (actions.size() <= 1) {
            return Collections.emptyList();
        }

        //copy all the actions because we must change the global order and not just move our "next param" action to the front
        //if the param info is visible and code completion is called then the code completion must be preferred (as in IntelliJ)
        //to achieve this properly and not to break other, similar situations we move our action before tab, but not further
        List<AnAction> promotedActions = Lists.newLinkedList(actions);
        boolean removed = promotedActions.removeIf(action -> action instanceof KiteAction);
        if (!removed) {
            return Collections.emptyList();
        }

        int tabActionIndex = -1;
        for (int i = 0; i < promotedActions.size(); i++) {
            AnAction action = promotedActions.get(i);
            if (action instanceof TabAction) {
                tabActionIndex = i;
                break;
            }
        }

        tabActionIndex = tabActionIndex == -1 ? promotedActions.size() - 1 : tabActionIndex;

        //insert all kite actions before the tab action
        for (AnAction action : actions) {
            if (action instanceof KiteAction) {
                promotedActions.add(tabActionIndex, action);
            }
        }

        return promotedActions;
    }
}

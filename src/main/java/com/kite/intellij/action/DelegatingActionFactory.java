package com.kite.intellij.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.actionSystem.EditorAction;

import javax.annotation.Nonnull;

/**
 * A factory class to create action which switch between the kite/original action depending on the current context.
 *
  */
public final class DelegatingActionFactory {
    private DelegatingActionFactory() {
    }

    /**
     * Creates a delegating action for the original and the kite override. If the input actions are {@link EditorAction}s then a special
     * {@link KiteDelegatingAction} is returned. Editor actions need to be handled specially because this type of actions has special
     * treatment in IntelliJ's code base.
     * <p>
     * For all other actions a {@link KiteDelegatingAction} is returned.
     *
     * @param kiteAction     The action which implements the behaviour in Kite's supported file types
     * @param originalAction The original action as shipped by IntelliJ, the delegate falls back to it in a non-kite context
     * @return A new, delegating action
     */
    @Nonnull
    public static AnAction create(@Nonnull AnAction kiteAction, @Nonnull AnAction originalAction) {
        boolean unitTestMode = ApplicationManager.getApplication().isUnitTestMode();

        if (originalAction instanceof EditorAction) {
            if (!(kiteAction instanceof EditorAction)) {
                throw new IllegalStateException("Kite action must be an EditorAction: " + kiteAction.toString());
            }

            return new KiteDelegatingEditorAction((EditorAction) kiteAction, (EditorAction) originalAction);
        }

        return unitTestMode
                ? new MockKiteDelegatingAction(kiteAction, originalAction)
                : new KiteDelegatingAction(kiteAction, originalAction);
    }
}

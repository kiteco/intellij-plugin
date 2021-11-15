package com.kite.intellij.action;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.kite.intellij.action.signatureInfo.KiteNextParameterAction;
import com.kite.intellij.action.signatureInfo.KitePrevParameterAction;
import com.kite.intellij.action.signatureInfo.KiteSignatureInfoAction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This component replaces certain built-in actions with a delegating action. That action will
 * let our own action handle the events for supported file types (Python). For all other file types
 * the built-in action will be called.
 *
 * IntelliJ calls {@link AppLifecycleListener} only for IDE in production mode.
 * Therefore we're calling overrideActions() in our tests in the @Before setup method.
 *
  */
public class KiteActionOverrideLifecycleListener implements AppLifecycleListener, Disposable {
    //defined in LangExtensionPoints.xml of the IntelliJ distribution
    public static final String INTELLIJ_PARAMETER_INFO_ACTION_ID = "ParameterInfo";
    public static final String INTELLIJ_PREVIOUS_PARAMETER_ACTION_ID = "PrevParameter";
    public static final String INTELLIJ_NEXT_PARAMETER_ACTION_ID = "NextParameter";
    private static final Logger LOG = Logger.getInstance("#kite.action.override");

    @Override
    public void appFrameCreated(@NotNull List<String> commandLineArgs) {
        // 2020.1 asserts !isDispatchThread() in ActionManagerImpl
        if (!ApplicationManager.getApplication().isDispatchThread()) {
            overrideActions();
        } else {
            ApplicationManager.getApplication().executeOnPooledThread(this::overrideActions);
        }
    }

    @Override
    public void dispose() {
    }

    public void overrideActions() {
        overrideAction(INTELLIJ_PARAMETER_INFO_ACTION_ID, new KiteSignatureInfoAction());
        overrideAction(INTELLIJ_PREVIOUS_PARAMETER_ACTION_ID, new KitePrevParameterAction());
        overrideAction(INTELLIJ_NEXT_PARAMETER_ACTION_ID, new KiteNextParameterAction());
    }

    private void overrideAction(String actionId, AnAction kiteAction) {
        ActionManager actionManager = ActionManager.getInstance();
        AnAction builtinAction = actionManager.getAction(actionId);
        if (builtinAction != null) {
            actionManager.unregisterAction(actionId);

            actionManager.registerAction(actionId, DelegatingActionFactory.create(kiteAction, builtinAction));
        } else {
            LOG.warn("Could not override action for " + actionId);
        }
    }

}

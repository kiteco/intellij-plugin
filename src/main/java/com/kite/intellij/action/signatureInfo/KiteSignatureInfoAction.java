package com.kite.intellij.action.signatureInfo;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.BaseCodeInsightAction;
import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.openapi.actionSystem.PopupAction;
import com.intellij.openapi.project.DumbAware;
import com.kite.intellij.action.KiteAction;
import com.kite.intellij.action.KiteActionOverrideLifecycleListener;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Action handling the signature info lookup. This action is not registered, but used by {@link KiteActionOverrideLifecycleListener}.
 *
  */
@SuppressWarnings("ComponentNotRegistered")
public class KiteSignatureInfoAction extends BaseCodeInsightAction implements HintManagerImpl.ActionToIgnore, DumbAware, PopupAction, KiteAction, FallbackAwareAction {
    private final KiteShowSignatureInfoActionHandler handler = new KiteShowSignatureInfoActionHandler();

    public KiteSignatureInfoAction() {
        setEnabledInModalContext(true);
        setInjectedContext(true);
    }

    @Override
    public void setFallbackCallback(@Nonnull Consumer<Throwable> fallbackCallback) {
        handler.setFallbackCallback(fallbackCallback);
    }

    @Override
    public void resetFallbackCallback() {
        handler.resetFallbackCallback();
    }

    @Nonnull
    @Override
    protected CodeInsightActionHandler getHandler() {
        return handler;
    }
}

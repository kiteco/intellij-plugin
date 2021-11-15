package com.kite.intellij.action.signatureInfo;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Extension for the action handling within the Kite plugin.
 * Actions like {@link KiteShowSignatureInfoActionHandler} need to perform http requests in the background to avoid EDT blocking.
 * The result can only be made available by a callback, the consequence is that Exceptions to notify the action delegate
 * to fallback is not possible any more.
 * <p>
 * The caller of a {@link FallbackAwareAction} is reseponsible to set the fallback action callback
 * right before the action execution call is made. The caller should also handle the {@link com.kite.intellij.action.FallbackToOriginalException}
 * if the called action does not perform background actions.
 *
  */
public interface FallbackAwareAction {
    void setFallbackCallback(@Nonnull Consumer<Throwable> fallbackCallback);

    void resetFallbackCallback();
}

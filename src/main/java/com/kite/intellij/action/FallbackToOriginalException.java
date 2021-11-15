package com.kite.intellij.action;

/**
 * This exception signals that the action delegation should invoke the original action because the plugin's action failed.
 *
  * @see KiteDelegatingAction
 * @see com.kite.intellij.action.signatureInfo.SignaturePopupController
 */
public class FallbackToOriginalException extends RuntimeException {
    public FallbackToOriginalException(String message, Throwable cause) {
        super(message, cause);
    }
}

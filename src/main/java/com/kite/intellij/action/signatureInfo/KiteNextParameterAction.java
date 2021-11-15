package com.kite.intellij.action.signatureInfo;

import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.kite.intellij.action.KiteAction;

/**
 * Action to override the built-in "next parameter" action.
 *
  * @see KitePrevNextActionHandler
 */
@SuppressWarnings("ComponentNotRegistered")
public class KiteNextParameterAction extends EditorAction implements KiteAction {
    public KiteNextParameterAction() {
        super(new KitePrevNextActionHandler(ParamActionType.Next));
        setInjectedContext(true);
    }
}

package com.kite.intellij.action.signatureInfo;

import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.kite.intellij.action.KiteAction;

/**
 * Action to override the built-in "previous parameter" action.
 *
  * @see com.kite.intellij.action.KiteParamActionPromoter
 */
@SuppressWarnings("ComponentNotRegistered")
public class KitePrevParameterAction extends EditorAction implements KiteAction {
    public KitePrevParameterAction() {
        super(new KitePrevNextActionHandler(ParamActionType.Previous));
        setInjectedContext(true);
    }
}

package com.kite.intellij.action.signatureInfo;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.kite.intellij.action.KiteAction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Action handler which is able to select the next/previous parameter in a currently visible signature info panel.
 * <p>
 *
  * @see com.kite.intellij.action.KiteParamActionPromoter
 * @see KitePrevNextActionHandler
 */
public class KitePrevNextActionHandler extends EditorActionHandler implements KiteAction {
    private final ParamActionType actionType;

    KitePrevNextActionHandler(ParamActionType actionType) {
        this.actionType = actionType;
    }

    @Override
    protected boolean isEnabledForCaret(@Nonnull Editor editor, @Nonnull Caret caret, DataContext dataContext) {
        return SignatureInfoEditorTracker.currentlyVisibleController(editor) != null;
    }

    @Override
    protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
        SignaturePopupController visibleController = SignatureInfoEditorTracker.currentlyVisibleController(editor);
        if (visibleController == null) {
            return;
        }

        if (actionType.isNext()) {
            visibleController.moveToNextParameter();
        } else {
            visibleController.moveToPreviousParameter();
        }
    }
}

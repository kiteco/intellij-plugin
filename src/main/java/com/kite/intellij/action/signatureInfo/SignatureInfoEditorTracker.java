package com.kite.intellij.action.signatureInfo;

import com.google.common.collect.Lists;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

/**
 * Tracks the {@link SignaturePopupController}s in an editor.
 * An instance is stored in the editor user data to be retrieved later on.
 * If previous instances are already attached to the editor
 * then they will be disposed before a new active controller is registered.
 *
  */
public class SignatureInfoEditorTracker {
    private static final Key<LinkedList<SignaturePopupController>> EDITOR_CONTROLLERS_KEY = Key.create("kite.signatureControllers");

    /**
     * If there is a currently active, visible {@link SignaturePopupController} for the given editor then it will be returned
     * by this method.
     *
     * @param editor The editor to check
     * @return An instance of {@link SignaturePopupController} which can be used by the caller
     */
    @Nullable
    public static SignaturePopupController currentlyVisibleController(@Nonnull Editor editor) {
        List<SignaturePopupController> controllers = EDITOR_CONTROLLERS_KEY.get(editor);
        if (controllers != null) {
            for (SignaturePopupController controller : controllers) {
                if (controller.isPopupVisible() || (ApplicationManager.getApplication().isUnitTestMode() && !controller.isDisposed())) {
                    return controller;
                }
            }

        }

        return null;
    }

    /**
     * Registers a new {@link SignaturePopupController} for an editor. Any previously registered instances will be closed (if necessary) and disposed.
     * If you'd like to re-use a currently visible controller please use {@link #currentlyVisibleController(Editor)}.
     *
     * @param editor          The controller will be stored in this editor's user data
     * @param popupController The controller to register in the editor
     */
    public static void register(@Nonnull Editor editor, @Nonnull SignaturePopupController popupController) {
        disposeAll(editor);

        LinkedList<SignaturePopupController> controllers = EDITOR_CONTROLLERS_KEY.get(editor);
        if (controllers == null) {
            controllers = Lists.newLinkedList();
            EDITOR_CONTROLLERS_KEY.set(editor, controllers);
        }

        // the list is already attached to the editor (see above)
        // add at the beginning to let it be found more quickly by the currentlyVisibleController method
        controllers.addFirst(popupController);
    }

    /**
     * Disposes all {@link SignaturePopupController} instances which are currently registered for the given editor.
     * If a popup of one of the controllers is currently visible then it will be closed before the controller is disposed.
     *
     * @param editor The editor to use
     */
    public static void disposeAll(@Nonnull Editor editor) {
        List<SignaturePopupController> controllers = EDITOR_CONTROLLERS_KEY.get(editor);
        if (controllers != null) {
            for (SignaturePopupController controller : controllers) {
                if (controller.isPopupVisible()) {
                    controller.closePopup();
                }

                Disposer.dispose(controller);
            }

            EDITOR_CONTROLLERS_KEY.set(editor, null);
        }
    }
}

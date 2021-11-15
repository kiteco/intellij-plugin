package com.kite.intellij.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ShortcutSet;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static com.kite.intellij.action.KiteActionUtils.isSupported;

/**
 * Simple wrapper to allow a delegating editor action. {@link EditorAction} has final methods, so we can't implement
 * a direct delegate for instances of this class.
 *
  */
public class KiteDelegatingEditorAction extends EditorAction implements KiteAction {
    private final EditorAction kiteAction;
    private final EditorAction fallbackAction;

    public KiteDelegatingEditorAction(EditorAction kiteAction, EditorAction fallbackAction) {
        this(new KiteDelegatingActionHandler(kiteAction, fallbackAction), kiteAction, fallbackAction);
    }

    public KiteDelegatingEditorAction(KiteDelegatingActionHandler kiteDelegatingActionHandler, EditorAction kiteAction, EditorAction fallbackAction) {
        super(kiteDelegatingActionHandler);

        this.kiteAction = kiteAction;
        this.fallbackAction = fallbackAction;

        this.kiteAction.copyFrom(this.fallbackAction);
    }

    @Override
    public void setInjectedContext(boolean worksInInjected) {
        kiteAction.setInjectedContext(worksInInjected);
        fallbackAction.setInjectedContext(worksInInjected);
    }

    @Override
    public void update(Editor editor, Presentation presentation, DataContext dataContext) {
        if (KiteActionUtils.isSupported(dataContext)) {
            kiteAction.update(editor, presentation, dataContext);
        } else {
            fallbackAction.update(editor, presentation, dataContext);
        }
    }

    @Override
    public void updateForKeyboardAccess(Editor editor, Presentation presentation, DataContext dataContext) {
        if (KiteActionUtils.isSupported(dataContext)) {
            kiteAction.updateForKeyboardAccess(editor, presentation, dataContext);
        } else {
            fallbackAction.updateForKeyboardAccess(editor, presentation, dataContext);
        }
    }

    @Override
    public void update(AnActionEvent e) {
        if (KiteActionUtils.isSupported(e)) {
            kiteAction.update(e);
        } else {
            fallbackAction.update(e);
        }
    }

    @Override
    public boolean displayTextInToolbar() {
        return fallbackAction.displayTextInToolbar();
    }

    @Override
    public void beforeActionPerformedUpdate(@Nonnull AnActionEvent e) {
        if (isSupported(e)) {
            kiteAction.beforeActionPerformedUpdate(e);
        } else {
            fallbackAction.beforeActionPerformedUpdate(e);
        }
    }

    @Override
    protected void setShortcutSet(@NotNull ShortcutSet shortcutSet) {
        //the shortcut must not be delegated because this breaks shortcuts in 163.x (no invocation of the action if the shortcut is pressed)
        //in that branch the registered action must have the shortcut itself, it seems
        super.setShortcutSet(shortcutSet);
    }

    @Override
    public boolean isDumbAware() {
        return fallbackAction.isDumbAware();
    }

    @Override
    public String toString() {
        return "KiteDelegatingEditorAction{" +
                "kiteAction=" + kiteAction +
                ", fallbackAction=" + fallbackAction +
                '}';
    }
}

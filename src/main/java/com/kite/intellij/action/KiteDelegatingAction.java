package com.kite.intellij.action;

import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.ShortcutSet;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.kite.intellij.action.signatureInfo.FallbackAwareAction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

import static com.kite.intellij.action.KiteActionUtils.isSupported;

/**
 * This action delegates to one of two actions, depending on a condition.
 * If the context doesn't allow to decide the delegate then the fallback will be preferred.
 * setter-methods will be delegated to both actions.
 *
  */
@SuppressWarnings("ComponentNotRegistered")
public class KiteDelegatingAction extends AnAction implements KiteAction {
    private static final Logger LOG = Logger.getInstance("#kite.action.delegate");

    protected final AnAction kiteAction;
    protected final AnAction fallbackAction;

    public KiteDelegatingAction(@Nonnull AnAction kiteAction, @Nonnull AnAction fallbackAction) {
        super(fallbackAction.getTemplatePresentation().getText(),
                fallbackAction.getTemplatePresentation().getDescription(),
                fallbackAction.getTemplatePresentation().getIcon());

        this.kiteAction = kiteAction;
        this.fallbackAction = fallbackAction;

        this.kiteAction.copyFrom(fallbackAction);
    }

    @Override
    public boolean displayTextInToolbar() {
        return fallbackAction.displayTextInToolbar();
    }

    @Override
    public void update(AnActionEvent e) {
        if (isSupported(e)) {
            kiteAction.update(e);
        } else {
            fallbackAction.update(e);
        }
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
    public void actionPerformed(AnActionEvent e) {
        if (isSupported(e)) {
            final Project project = e.getProject();
            final Editor editor = project != null ? CommonDataKeys.EDITOR.getData(e.getDataContext()) : null;

            Consumer<Throwable> fallbackHandler = ex -> runFallback(e, project, editor, ex);

            //atm it's only possible to do an async action fallback with CodeInsightActions
            boolean useFallbackCallback = kiteAction instanceof FallbackAwareAction && fallbackAction instanceof CodeInsightAction;
            try {
                if (useFallbackCallback) {
                    ((FallbackAwareAction) kiteAction).setFallbackCallback(fallbackHandler);
                }

                kiteAction.actionPerformed(e);
            } catch (FallbackToOriginalException ex) {
                fallbackHandler.accept(ex);
            } finally {
                if (useFallbackCallback) {
                    ((FallbackAwareAction) kiteAction).resetFallbackCallback();
                }
            }
        } else {
            fallbackAction.actionPerformed(e);
        }
    }

    @Override
    protected void setShortcutSet(@NotNull ShortcutSet shortcutSet) {
        //the shortcut must not be delegated because this breaks shortcuts in 163.x (no invocation of the action if the shortcut is pressed)
        //in that branch the registered action must have the shortcut itself, it seems
        super.setShortcutSet(shortcutSet);
    }

    private void runFallback(AnActionEvent e, Project project, Editor editor, Throwable exception) {
        if (exception instanceof FallbackToOriginalException) {
            LOG.debug("Fallback to IntelliJ action", exception);

            if (fallbackAction instanceof CodeInsightAction && project != null && editor != null) {
                ((CodeInsightAction) fallbackAction).actionPerformedImpl(project, editor);
            } else {
                fallbackAction.actionPerformed(e);
            }
        }
    }

    @Override
    public void setDefaultIcon(boolean isDefaultIconSet) {
        kiteAction.setDefaultIcon(isDefaultIconSet);
        fallbackAction.setDefaultIcon(isDefaultIconSet);
    }

    @Override
    public boolean isDefaultIcon() {
        return fallbackAction.isDefaultIcon();
    }

    @Override
    public void setInjectedContext(boolean worksInInjected) {
        kiteAction.setInjectedContext(worksInInjected);
        fallbackAction.setInjectedContext(worksInInjected);
    }

    @Override
    public boolean isInInjectedContext() {
        return fallbackAction.isInInjectedContext();
    }

    @Override
    public boolean isTransparentUpdate() {
        return fallbackAction.isTransparentUpdate();
    }

    @Override
    public boolean isDumbAware() {
        return fallbackAction.isDumbAware();
    }

    @Override
    public String toString() {
        return "KiteDelegatingAction{" +
                "kiteAction=" + kiteAction +
                ", fallbackAction=" + fallbackAction +
                '}';
    }

}

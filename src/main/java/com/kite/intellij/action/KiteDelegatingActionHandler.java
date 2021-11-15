package com.kite.intellij.action;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.DocCommandGroupId;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.kite.intellij.action.signatureInfo.FallbackAwareAction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static com.kite.intellij.action.KiteActionUtils.isSupported;

/**
 * A delegating action handler which delegates to our own kite action handler implementation if the current file/context is supported by Kite.
 *
  */
@SuppressWarnings("deprecation")
public class KiteDelegatingActionHandler extends EditorActionHandler {
    private static final Logger LOG = Logger.getInstance("#kite.action.delegateHandler");

    protected final EditorActionHandler kiteActionHandler;
    protected final EditorActionHandler originalActionHandler;

    public KiteDelegatingActionHandler(EditorAction kiteAction, EditorAction originalAction) {
        super(originalAction.getHandler().runForAllCarets());

        this.originalActionHandler = originalAction.getHandler();
        this.kiteActionHandler = kiteAction.getHandler();
    }

    @Override
    public boolean isEnabled(Editor editor, DataContext dataContext) {
        if (isSupported(dataContext)) {
            return kiteActionHandler.isEnabled(editor, dataContext);
        }

        return originalActionHandler.isEnabled(editor, dataContext);
    }

    @Override
    public void execute(@NotNull Editor editor, DataContext dataContext) {
        execute(editor, null, dataContext);
    }

    @Override
    protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
        if (isSupported(dataContext)) {
            Consumer<Throwable> fallbackCallback = e -> {
                if (e instanceof FallbackToOriginalException) {
                    LOG.debug("Fallback to IntelliJ action handler", e);
                    runOriginalHandler(editor, caret, dataContext);
                }
            };

            try {
                if (kiteActionHandler instanceof FallbackAwareAction) {
                    ((FallbackAwareAction) kiteActionHandler).setFallbackCallback(fallbackCallback);
                }

                kiteActionHandler.execute(editor, dataContext);
            } catch (FallbackToOriginalException e) {
                fallbackCallback.accept(e);
            } finally {
                if (kiteActionHandler instanceof FallbackAwareAction) {
                    ((FallbackAwareAction) kiteActionHandler).resetFallbackCallback();
                }
            }
        } else {
            runOriginalHandler(editor, caret, dataContext);
        }
    }

    @Override
    public boolean executeInCommand(@NotNull Editor editor, DataContext dataContext) {
        if (isSupported(dataContext)) {
            return kiteActionHandler.executeInCommand(editor, dataContext);
        }

        return originalActionHandler.executeInCommand(editor, dataContext);
    }

    @Override
    public boolean runForAllCarets() {
        return originalActionHandler.runForAllCarets();
    }

    @Override
    public DocCommandGroupId getCommandGroupId(@NotNull Editor editor) {
        return originalActionHandler.getCommandGroupId(editor);
    }

    protected void runOriginalHandler(Editor editor, @Nullable Caret caret, DataContext dataContext) {
        originalActionHandler.execute(editor, caret, dataContext);
    }
}

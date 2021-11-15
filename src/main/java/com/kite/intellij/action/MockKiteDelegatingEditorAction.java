package com.kite.intellij.action;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.Nullable;

/**
 * Mock implementation returned by {@link DelegatingActionFactory} when run in unit testing environment.
  */
@SuppressWarnings("ComponentNotRegistered")
@TestOnly
public class MockKiteDelegatingEditorAction extends KiteDelegatingEditorAction {
    public MockKiteDelegatingEditorAction(EditorAction kiteAction, EditorAction fallbackAction) {
        super(new MockKiteDelegatingActionHandler(kiteAction, fallbackAction), kiteAction, fallbackAction);
    }

    public boolean isFallbackPerformed() {
        return ((MockKiteDelegatingActionHandler) getHandler()).fallbackPerformed;
    }

    private static class MockKiteDelegatingActionHandler extends KiteDelegatingActionHandler {
        volatile boolean fallbackPerformed = false;

        public MockKiteDelegatingActionHandler(EditorAction kiteAction, EditorAction fallbackAction) {
            super(kiteAction, fallbackAction);
        }

        @Override
        protected void runOriginalHandler(Editor editor, @Nullable Caret caret, DataContext dataContext) {
            super.runOriginalHandler(editor, caret, dataContext);

            fallbackPerformed = true;
        }
    }
}

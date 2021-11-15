package com.kite.intellij.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.Nonnull;

import static com.kite.intellij.action.KiteActionUtils.isSupported;

/**
 * Mock implementation returned by {@link DelegatingActionFactory} when run in unit testing environment.
 *
  */
@TestOnly
public class MockKiteDelegatingAction extends KiteDelegatingAction {
    @TestOnly
    private volatile boolean fallbackPerformed = false;

    public MockKiteDelegatingAction(@Nonnull AnAction kiteAction, @Nonnull AnAction fallbackAction) {
        super(kiteAction, fallbackAction);
    }

    @TestOnly
    public boolean isFallbackPerformed() {
        return fallbackPerformed;
    }

    @TestOnly
    public void resetTestData() {
        fallbackPerformed = false;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        if (isSupported(e)) {
            try {
                kiteAction.actionPerformed(e);
            } catch (FallbackToOriginalException ex) {
                fallbackPerformed = true;
                fallbackAction.actionPerformed(e);
            }
        } else {
            fallbackAction.actionPerformed(e);
        }
    }
}

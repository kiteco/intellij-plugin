package com.kite.intellij;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;

import javax.annotation.Nonnull;

/**
 * Empty service, which can be used as a disposable parent to dispose elements when a project is closed.
 */
public class KiteProjectLifecycleService implements Disposable {
    @SuppressWarnings("MissingRecentApi")
    public static Disposable getInstance(@Nonnull Project project) {
        return project.getService(KiteProjectLifecycleService.class);
    }

    @Override
    public void dispose() {
    }
}

package com.kite.intellij.lang.documentation;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

import javax.annotation.Nonnull;

/**
 * Project service providing access to shared renderer instances. It keeps singletons to be accessed by actions and other
 * components.
 *
  */
public class KiteDocumentationRendererService implements Disposable {
    private final PebbleDocumentationRenderer detailedRenderer;

    public KiteDocumentationRendererService() {
        detailedRenderer = new PebbleDocumentationRenderer();
        Disposer.register(this, detailedRenderer);
    }

    @Nonnull
    public static KiteDocumentationRendererService getInstance(Project project) {
        return project.getService(KiteDocumentationRendererService.class);
    }

    public KiteDocumentationRenderer getDetailedRenderer() {
        return detailedRenderer;
    }

    @Override
    public void dispose() {
    }
}

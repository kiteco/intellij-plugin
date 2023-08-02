package com.kite.intellij.editor.events;

import com.google.common.collect.Maps;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.util.Disposer;
import com.kite.intellij.KiteProjectLifecycleService;
import com.kite.intellij.platform.KitePlatform;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Listens to IntelliJ's events and executes the coresponding Kite events whenever necessary.
 * <p>
 * It will only react to events if the current platform is supported by Kite.
 *
 */
public class DefaultEditorEventListener implements ProjectActivity {
    private final Map<Project, ProjectEditorEventListener> listenerByProject = Maps.newHashMap();

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        if (!KitePlatform.isOsVersionSupported()) {
            return null;
        }

        if (!listenerByProject.containsKey(project)){
            listenerByProject.put(project, new ProjectEditorEventListener(project));
        }

        ProjectEditorEventListener projectEditorEventListener = listenerByProject.get(project);
        Disposer.register(project.getService(KiteProjectLifecycleService.class), projectEditorEventListener);

        projectEditorEventListener.execute();

        return null;
    }
}

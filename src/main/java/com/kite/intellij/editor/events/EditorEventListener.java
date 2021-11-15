package com.kite.intellij.editor.events;

import com.intellij.openapi.project.Project;

/**
 * Listens to editor interaction and sends Kite events for these.
 * <p>
 * The implementation must be careful to not send out too many {@link KiteEvent}s.
 *
  */
public interface EditorEventListener {
    static EditorEventListener getInstance(Project project) {
        return project.getComponent(EditorEventListener.class);
    }

    /**
     * @return Returns the event queue used by this editor event listener.
     */
    KiteEventQueue getEventQueue();

    /**
     * Waits until all events queued to be send to the queue were processed. This method only waits if it
     * is called from the Swing event dispatch thread.
     */
    void awaitEvents();
}

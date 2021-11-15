package com.kite.intellij.editor.events;

import com.intellij.openapi.editor.Document;
import com.kite.intellij.backend.KiteApiService;
import com.kite.intellij.backend.http.HttpTimeoutConfig;
import com.kite.intellij.backend.model.EventType;
import com.kite.intellij.platform.fs.CanonicalFilePath;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Defines an event which may be send to the kite backend.
 * An event may override previous events if it contains at least the same data as the previous event and
 * if the previous event is not strictly necessary for Kite to work.
 *
  */
public interface KiteEvent {
    EventType getType();

    /**
     * Sends the event to the kite api.
     *
     * @param api
     * @return {@link true} if the event was successfully send and processed by the kite api, {@code false} otherwise.
     * @see com.kite.intellij.backend.KiteApiService#sendEvent(EventType, CanonicalFilePath, String, List, boolean, HttpTimeoutConfig)
     */
    boolean send(@Nonnull KiteApiService api);

    /**
     * @param previous An event which was triggered previously, it must not be the event immediately the current event (represented by {@code this})
     * @return {@code true} if this event overrides the given event which was triggered previously.
     */
    boolean isOverriding(KiteEvent previous);

    /**
     * @return The canonical file path for which this event was triggered.
     */
    @Nonnull
    CanonicalFilePath getFilePath();

    /**
     * @return The content of the file given in {@link #getFilePath()}
     */
    @Nonnull
    String getContent();

    /**
     * @return The document representing the file in {@link #getFilePath()}
     */
    @Nullable
    Document getDocument();
}

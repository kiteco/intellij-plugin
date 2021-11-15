package com.kite.intellij.editor.events;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.kite.intellij.backend.KiteApiService;
import com.kite.intellij.backend.http.HttpTimeoutConfig;
import com.kite.intellij.backend.http.KiteHttpException;
import com.kite.intellij.backend.model.EventType;
import com.kite.intellij.backend.model.TextSelection;
import com.kite.intellij.platform.fs.CanonicalFilePath;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;

/**
 * Abstract base class for {@link KiteEvent} implementations. The override logic in {@link #isOverriding(KiteEvent)} needs to be implemented by subclasses.
 *
  */
abstract class AbstractKiteEvent implements KiteEvent {
    private static final Logger LOG = Logger.getInstance("#kite.eventQueue");

    protected final Document document;
    protected final CanonicalFilePath filePath;
    protected final String content;
    protected final TextSelection selection;
    protected final EventType eventType;
    protected final boolean statusNotificationsEnabled;

    public AbstractKiteEvent(EventType eventType, CanonicalFilePath filePath, String content, TextSelection selection, Document document, boolean statusNotificationsEnabled) {
        this.document = document;
        this.filePath = filePath;
        this.content = content;
        this.selection = selection;
        this.eventType = eventType;
        this.statusNotificationsEnabled = statusNotificationsEnabled;
    }

    @Override
    public EventType getType() {
        return eventType;
    }

    @Override
    public boolean send(@NotNull KiteApiService api) {
        try {
            return api.sendEvent(eventType, filePath, content, Collections.singletonList(selection), statusNotificationsEnabled, HttpTimeoutConfig.ShortTimeout);
        } catch (KiteHttpException e) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Sending event failed with HTTP status error: " + e.getMessage());
            }
            return false;
        } catch (Exception e) {
            if (ApplicationManager.getApplication().isUnitTestMode()) {
                LOG.error("Sending event failed with HTTP status error", e);
            } else if (LOG.isTraceEnabled()) {
                LOG.trace("Sending event failed with HTTP status error: " + e.getMessage());
            }
            return false;
        }
    }

    @Nonnull
    @Override
    public CanonicalFilePath getFilePath() {
        return filePath;
    }

    @Nonnull
    @Override
    public String getContent() {
        return content;
    }

    @Nullable
    @Override
    public Document getDocument() {
        return document;
    }
}

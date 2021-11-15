package com.kite.intellij.editor.events;

import com.intellij.openapi.editor.Document;
import com.kite.intellij.backend.KiteApiService;
import com.kite.intellij.backend.model.EventType;
import com.kite.intellij.backend.model.TextSelection;
import com.kite.intellij.platform.fs.CanonicalFilePath;

/**
 * Selection event to notify about cursor position changes in a file.
 * <p>
 * A selection event overrides previous selection events triggered for the same file.
 *
  */
final class SelectionEvent extends AbstractKiteEvent {
    SelectionEvent(Document document, CanonicalFilePath filePath, String content, TextSelection selection, boolean statusNotificationsEnabled) {
        super(EventType.SELECTION, filePath, content, selection, document, statusNotificationsEnabled);
    }

    @Override
    public String toString() {
        return "SelectionEvent{}";
    }

    @Override
    public boolean isOverriding(KiteEvent previous) {
        return previous != this
                && (previous.getType() == EventType.SELECTION || previous.getType() == EventType.EDIT)
                && filePath.equals(previous.getFilePath());
    }
}

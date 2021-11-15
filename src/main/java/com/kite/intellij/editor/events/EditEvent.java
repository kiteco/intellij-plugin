package com.kite.intellij.editor.events;

import com.intellij.openapi.editor.Document;
import com.kite.intellij.backend.KiteApiService;
import com.kite.intellij.backend.model.EventType;
import com.kite.intellij.backend.model.TextSelection;
import com.kite.intellij.platform.fs.CanonicalFilePath;

/**
 * Defines an edit event.
 * <p>
 * An edit event overrides a selection event.
 *
  */
final class EditEvent extends AbstractKiteEvent {
    EditEvent(Document document, CanonicalFilePath filePath, String content, int cursorOffset, boolean statusNotificationsEnabled) {
        super(EventType.EDIT, filePath, content, TextSelection.create(cursorOffset), document, statusNotificationsEnabled);
    }

    EditEvent(Document document, CanonicalFilePath filePath, String content, TextSelection selection, boolean statusNotificationsEnabled) {
        super(EventType.EDIT, filePath, content, selection, document, statusNotificationsEnabled);
    }

    @Override
    public String toString() {
        return "EditEvent{}";
    }

    @Override
    public boolean isOverriding(KiteEvent previous) {
        return previous != this
                && (previous.getType() == EventType.EDIT || previous.getType() == EventType.SELECTION)
                && filePath.equals(previous.getFilePath());
    }
}

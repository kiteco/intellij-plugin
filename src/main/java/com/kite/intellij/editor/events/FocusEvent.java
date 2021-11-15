package com.kite.intellij.editor.events;

import com.intellij.openapi.editor.Document;
import com.kite.intellij.backend.model.EventType;
import com.kite.intellij.backend.model.TextSelection;
import com.kite.intellij.platform.fs.CanonicalFilePath;

/**
 * A focus event, triggered when a new file is opened or when a file is focused.
 *
  */
class FocusEvent extends AbstractKiteEvent {
    FocusEvent(Document document, CanonicalFilePath filePath, String content, int cursorOffset, boolean statusNotificationsEnabled) {
        super(EventType.FOCUS, filePath, content, TextSelection.create(cursorOffset), document, statusNotificationsEnabled);
    }

    FocusEvent(Document document, CanonicalFilePath filePath, String content, TextSelection selection, boolean statusNotificationsEnabled) {
        super(EventType.FOCUS, filePath, content, selection, document, statusNotificationsEnabled);
    }

    @Override
    public boolean isOverriding(KiteEvent previous) {
        return previous != this
                && (previous.getType() == EventType.SELECTION || previous.getType() == EventType.EDIT)
                && filePath.equals(previous.getFilePath());
    }

    @Override
    public String toString() {
        return "FocusEvent{}";
    }
}

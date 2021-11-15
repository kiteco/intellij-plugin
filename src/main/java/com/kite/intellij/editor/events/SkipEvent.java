package com.kite.intellij.editor.events;

import com.intellij.openapi.editor.Document;
import com.kite.intellij.backend.KiteApiService;
import com.kite.intellij.backend.model.EventType;
import com.kite.intellij.backend.model.TextSelection;
import com.kite.intellij.platform.fs.CanonicalFilePath;

/**
 * Skip event to notify Kite about a skipped event because the content is too large.
 *
  */
final class SkipEvent extends AbstractKiteEvent {
    SkipEvent(Document document, CanonicalFilePath filePath, TextSelection selection) {
        super(EventType.SKIP, filePath, "", selection, document, false);
    }

    @Override
    public String toString() {
        return "SkipEvent{}";
    }

    @Override
    public boolean isOverriding(KiteEvent previous) {
        return previous != this
                && previous.getType() == EventType.SKIP
                && filePath.equals(previous.getFilePath());
    }
}

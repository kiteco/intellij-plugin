package com.kite.intellij.editor.events;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.kite.intellij.backend.KiteApiService;
import com.kite.intellij.backend.model.EventType;
import com.kite.intellij.backend.model.TextSelection;
import com.kite.intellij.editor.util.FileEditorUtil;
import com.kite.intellij.lang.KiteLanguageSupport;
import com.kite.intellij.platform.fs.CanonicalFilePath;
import com.kite.intellij.platform.fs.UnixCanonicalPath;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Factory to create new kite events.
 *
  */
class KiteEventFactory {
    public static final KiteEvent DUMMY_EVENT = new AbstractKiteEvent(EventType.EDIT, new UnixCanonicalPath("dummyevent.py"), "", TextSelection.create(0, 0), null, false) {
        @Override
        public boolean send(@NotNull KiteApiService api) {
            // do nothing
            return true;
        }

        @Override
        public boolean isOverriding(KiteEvent previous) {
            return false;
        }
    };

    static KiteEvent create(@Nonnull EventType type, @Nonnull CanonicalFilePath filePath, @Nonnull Editor editor) {
        boolean supported = KiteLanguageSupport.isSupported(editor, KiteLanguageSupport.Feature.BasicSupport);
        String content = FileEditorUtil.contentOf(editor);
        TextSelection range = TextSelection.create(editor.getCaretModel().getOffset());
        return create(type, filePath, content, range, editor.getDocument(), supported);
    }

    static KiteEvent create(@Nonnull EventType type, @Nonnull CanonicalFilePath filePath, @Nonnull String content, @Nonnull TextSelection selection, @Nullable Document document, boolean statusNotificationsEnabled) {
        switch (type) {
            case EDIT:
                return new EditEvent(document, filePath, content, selection, statusNotificationsEnabled);
            case FOCUS:
                return new FocusEvent(document, filePath, content, selection, statusNotificationsEnabled);
            case SELECTION:
                return new SelectionEvent(document, filePath, content, selection, statusNotificationsEnabled);
            case SKIP:
                return new SkipEvent(document, filePath, selection);
            default:
                throw new IllegalStateException("Unhandled event type " + type);
        }
    }

    static KiteEvent createSkipEvent(CanonicalFilePath filePath) {
        return create(EventType.SKIP, filePath, "", TextSelection.create(0), null, false);
    }
}

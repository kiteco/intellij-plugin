package com.kite.intellij.backend.model;

/**
 */
public enum EventType {
    FOCUS, EDIT, SELECTION, SKIP;

    public String asKiteId() {
        switch (this) {
            case EDIT:
                return "edit";

            case FOCUS:
                return "focus";

            case SELECTION:
                return "selection";

            case SKIP:
                return "skip";

            default:
                throw new IllegalStateException("Unhandled event type" + this);
        }
    }
}

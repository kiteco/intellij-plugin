package com.kite.intellij.ui.notifications;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;

import java.time.Duration;
import java.util.Optional;

/**
 * Helper to simplify displaying balloon notification messages.
 *
  */
public class KiteBalloonNotifications {
    private KiteBalloonNotifications() {
    }

    public static void showNotificationAtEditorOffset(String message, MessageType messageType, int editorOffset, Optional<Duration> fadeoutDelay, Editor editor) {
        showNotificationAtEditorPosition(message, messageType, editor.offsetToLogicalPosition(editorOffset), fadeoutDelay, editor);
    }

    public static void showNotificationAtEditorPosition(String message, MessageType messageType, LogicalPosition position, Optional<Duration> fadeoutDelay, Editor editor) {
        BalloonBuilder builder = JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(message, messageType, null);

        //apply optional fadeout
        fadeoutDelay.ifPresent(duration -> builder.setFadeoutTime(duration.toMillis()));

        Balloon balloon = builder.createBalloon();
        balloon.show(new RelativePoint(editor.getContentComponent(), editor.logicalPositionToXY(position)), Balloon.Position.above);
    }
}

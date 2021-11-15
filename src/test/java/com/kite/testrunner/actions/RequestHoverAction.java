package com.kite.testrunner.actions;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseEventArea;
import com.intellij.openapi.editor.ex.EditorSettingsExternalizable;
import com.kite.intellij.platform.fs.CanonicalFilePath;
import com.kite.intellij.platform.fs.CanonicalFilePathFactory;
import com.kite.intellij.settings.KiteSettings;
import com.kite.intellij.settings.KiteSettingsService;
import com.kite.testrunner.TestContext;
import com.kite.testrunner.TestFailedException;
import com.kite.testrunner.TestRunnerUtil;

import java.awt.*;
import java.awt.event.MouseEvent;

public class RequestHoverAction implements TestAction {
    @Override
    public String getId() {
        return "request_hover";
    }

    @Override
    public void run(TestContext context) throws Throwable {
        try {
            // we have to flush the kited events to get an up-to-date whitelist status mapping
            TestRunnerUtil.flushEvents(context);
        } catch (Exception e) {
            throw new TestFailedException(context, "flushing events failed", e);
        }

        // we don't have a quick-doc action anymore, so we have to manually request hover data to emulate the mouse-over behavior
        Editor editor = context.getFixture().getEditor();
        CanonicalFilePath path = CanonicalFilePathFactory.getInstance().createFor(editor, CanonicalFilePathFactory.Context.Event);

        // we emulate a user's mouse move and hover over the token at the current caret's position
        VisualPosition visualPos = editor.offsetToVisualPosition(editor.getCaretModel().getOffset());
        Point point = editor.visualPositionToXY(visualPos);
        MouseEvent ev = new MouseEvent(editor.getContentComponent(), 1, System.currentTimeMillis(), 0, point.x, point.y, 0, false);
        EditorMouseEvent mouseEvent = new EditorMouseEvent(editor, ev, EditorMouseEventArea.EDITING_AREA);

        // force a custom delay of 0ms to make the request quickly
        KiteSettings kiteState = KiteSettingsService.getInstance().getState();

        // enable IntelliJ's mouse-over feature
        EditorSettingsExternalizable ideSettings = EditorSettingsExternalizable.getInstance();
        boolean oldValue = ideSettings.isShowQuickDocOnMouseOverElement();
        try {
            ideSettings.setShowQuickDocOnMouseOverElement(true);
        } finally {
            ideSettings.setShowQuickDocOnMouseOverElement(oldValue);
        }
    }
}

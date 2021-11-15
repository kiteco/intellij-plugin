package com.kite.intellij.status;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.util.Key;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;

/**
 * The widget which displays the detailed information about Kite's current status.
 *
  */
public class KiteStatusPopupController {
    private static final Key<JBPopup> CURRENT_POPUP_KEY = Key.create("kite.statusPopup");

    public KiteStatusPopupController() {
    }

    public void show(KiteStatusBarWidget owner, Project project, KiteStatusModel model) {
        KiteStatusPanelForm form = new KiteStatusPanelForm(model, project);
        form.update(project);

        JPanel popupContent = form.getBasePanel();

        Dimension dimension = popupContent.getPreferredSize();
        RelativePoint pos = new RelativePoint(owner.getComponent(),
                new Point(-dimension.width + owner.getComponent().getWidth() / 2, -dimension.height));

        JBPopup popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(popupContent, popupContent)
                .setProject(project)
                .setMovable(false)
                .setResizable(false)
                .setShowShadow(false)
                .addListener(new JBPopupListener() {
                    @Override
                    public void onClosed(@NotNull LightweightWindowEvent event) {
                        CURRENT_POPUP_KEY.set(project, null);
                    }
                })
                .createPopup();

        CURRENT_POPUP_KEY.set(project, popup);
        popup.show(pos);
    }

    @Nullable
    public JBPopup getCurrentPopup(Project project) {
        JBPopup popup = CURRENT_POPUP_KEY.get(project);

        if (popup != null && popup.isDisposed()) {
            CURRENT_POPUP_KEY.set(project, null);
            return null;
        }

        return popup;
    }
}

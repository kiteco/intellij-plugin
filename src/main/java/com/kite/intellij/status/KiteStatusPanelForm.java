package com.kite.intellij.status;

import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.ui.components.labels.LinkListener;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.UIUtil;
import com.kite.intellij.Icons;
import com.kite.intellij.settings.KiteConfigurable;
import com.kite.intellij.ui.KiteLinkLabel;

import javax.swing.*;
import java.awt.*;

public class KiteStatusPanelForm {
    private static final BrowseLinkListener OPEN_LINK_LISTENER = new BrowseLinkListener();
    private final LinkListener<String> PLUGIN_SETTINGS_LINK_LISTENER;

    private final KiteStatusModel model;
    @SuppressWarnings("unused")
    private JLabel headerIcon;
    @SuppressWarnings("unused")
    private LinkLabel<String> headingAccountActionLabel;
    private JPanel basePanel;
    private JComponent statusIcon;
    private JBLabel statusLabel;
    private JBLabel detailedStatusLabel;
    private LinkLabel<String> helpLink;
    private LinkLabel<String> settingsLink;
    private LinkLabel<String> pluginSettingsLink;
    private LinkLabel<String> searchPythonDocsLabel;
    private LinkLabel<String> shareKiteLabel;
    private JButton statusActionButton;
    private JBLabel productLabel;
    private JBLabel productDetailsLabel;

    public KiteStatusPanelForm(KiteStatusModel model, Project project) {
        this.model = model;

        PLUGIN_SETTINGS_LINK_LISTENER = (source, linkData) -> openPluginSettings(project);
    }

    public JPanel getBasePanel() {
        return basePanel;
    }

    public void update(Project project) {
        //#165 disable all menu items, which need a running Kite engine
        searchPythonDocsLabel.setEnabled(model.isMenuLinksEnabled());
        searchPythonDocsLabel.setPaintUnderline(model.isMenuLinksEnabled());

        String label = model.getProductLabelText();
        productLabel.setVisible(label != null);
        productLabel.setText(label);

        productDetailsLabel.setText(model.getProductDetailsLabelText());
        productDetailsLabel.setVisible(!model.getProductDetailsLabelText().isEmpty());

        // status label
        statusLabel.setText(model.getStatusLabelText());
        if (model.getStatusLabelColor() != null) {
            statusLabel.setForeground(model.getStatusLabelColor());
            if (model.isStatusLabelBold()) {
                statusLabel.setFont(JBFont.create(statusLabel.getFont(), false).asBold());
            }
        }
        statusIcon.setForeground(model.getStatusIconColor());

        detailedStatusLabel.setVisible(model.isDetailedStatusVisible());

        statusActionButton.setVisible(model.isStatusActionButtonVisible());
        if (statusActionButton.isVisible()) {
            statusActionButton.setText(model.getStatusActionButtonText());
            statusActionButton.addActionListener(e -> model.doStatusButtonAction(project));

            if (model.getStatusActionButtonColor() != null) {
                statusActionButton.setForeground(model.getStatusActionButtonColor());
            }
        }
    }

    /**
     * Jump to the Kite tab of the Preferences page.
     *
     * @param project The current project.
     */
    protected void openPluginSettings(Project project) {
        //hide the popup first because it may overlap the settings dialog (depends on the main window's size)
        KiteStatusBarWidget.hideCurrentPopup(project);

        ShowSettingsUtil.getInstance().showSettingsDialog(project, KiteConfigurable.class);
    }

    private void createUIComponents() {
        headerIcon = new JBLabel(Icons.StatusPanelLogo);

        // white on blue in light mode, black on white in dark mode
        this.productLabel = new KiteInvertedLabel(UIUtil.ComponentStyle.LARGE,
                new JBColor(ColorUtil.fromHex("#0005f9"), Color.white),
                JBColor.WHITE);

        headingAccountActionLabel = new KiteLinkLabel<>("", null, OPEN_LINK_LISTENER, model.getAccountActionUrl(), model.getAccountActionComponentStyle());

        shareKiteLabel = new KiteLinkLabel<>("", Icons.StatusPanelGift, OPEN_LINK_LISTENER, model.getShareKiteLinkURL(), model.getMenuComponentStyle());
        searchPythonDocsLabel = new KiteLinkLabel<>("", Icons.StatusPanelMagnifyingGlass, OPEN_LINK_LISTENER, model.getSearchPythonDocsUrl(), model.getMenuComponentStyle());
        helpLink = new KiteLinkLabel<>("", Icons.StatusPanelQuestionMark, OPEN_LINK_LISTENER, model.getHelpLinkUrl(), model.getMenuComponentStyle());
        settingsLink = new KiteLinkLabel<>("", Icons.StatusPanelControlDials, OPEN_LINK_LISTENER, model.getSettingsLinkUrl(), model.getMenuComponentStyle());
        pluginSettingsLink = new KiteLinkLabel<>("", Icons.StatusPanelControlDials, PLUGIN_SETTINGS_LINK_LISTENER, null, UIUtil.ComponentStyle.REGULAR);

        statusIcon = new ColoredCircleComponent();
    }
}

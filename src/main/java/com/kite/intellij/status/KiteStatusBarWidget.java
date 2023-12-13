package com.kite.intellij.status;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IconLikeCustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.ui.ClickListener;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.kite.intellij.backend.ConnectionStatusListener;
import com.kite.intellij.backend.KiteApiService;
import com.kite.intellij.backend.WebappLinks;
import com.kite.intellij.backend.http.HttpTimeoutConfig;
import com.kite.intellij.backend.model.KiteFileStatus;
import com.kite.intellij.backend.model.KiteFileStatusResponse;
import com.kite.intellij.backend.model.LicenseInfo;
import com.kite.intellij.backend.model.UserInfo;
import com.kite.intellij.lang.KiteLanguageSupport;
import com.kite.intellij.platform.KiteDetector;
import com.kite.intellij.platform.KiteInstallService;
import com.kite.intellij.platform.fs.CanonicalFilePath;
import com.kite.intellij.platform.fs.CanonicalFilePathFactory;
import com.kite.intellij.ui.SwingWorkerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.ide.PooledThreadExecutor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * The component which displays a Kite icon to indicate the current status.
 */
public class KiteStatusBarWidget extends EditorBasedWidget implements IconLikeCustomStatusBarWidget {
    private static final Logger LOG = Logger.getInstance("#kite.status.statusBarWidget");
    private static final Key<KiteStatusBarWidget> WIDGET_KEY = Key.create("kite.statusWidget");
    private final KiteStatusPopupController popupController = new KiteStatusPopupController();
    private final JLabel iconLabel;
    private final ConnectionStatusListener connectionStatusListener;
    private final Timer updateTimer = new Timer();
    private volatile boolean disposed;
    private volatile IconStatus currentIconStatus;

    public KiteStatusBarWidget(@NotNull Project project) {
        // this registers this widget with the Disposer
        super(project);
        WIDGET_KEY.set(project, this);

        iconLabel = new JBLabel();
        iconLabel.putClientProperty(UIUtil.CENTER_TOOLTIP_DEFAULT, Boolean.TRUE);
        iconLabel.setOpaque(true);
        //iconLabel.setBorder(WidgetBorder.ICON);

        connectionStatusListener = (connectionAvailable, error) -> {
            if (!connectionAvailable) {
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (project.isDisposed()) {
                        return;
                    }

                    SwingWorkerUtil.compute(
                            () -> computeIconStatus(null, Boolean.TRUE, KiteApiService.getInstance()),
                            this::updateStatusbarIcon
                    );
                });
            }
        };

        this.updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ApplicationManager.getApplication().invokeLater(() -> {
                    try {
                        refreshStatusIconAsync();
                    } catch (Exception e) {
                        //ignored
                    }
                });
            }
        }, TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(10));
    }

    public static void hideCurrentPopup(@Nonnull Project project) {
        KiteStatusBarWidget widget = WIDGET_KEY.get(project);
        if (widget != null) {
            widget.closePopup();
        }
    }

    public static void showCurrentPopup(@Nonnull Project project) {
        KiteStatusBarWidget widget = WIDGET_KEY.get(project);
        if (widget != null) {
            widget.openStatusPanel();
        }
    }

    @Override
    public JComponent getComponent() {
        return iconLabel;
    }

    @Nonnull
    @Override
    public String ID() {
        return "kite.status";
    }

    @Override
    public void install(@Nonnull StatusBar statusBar) {
        super.install(statusBar);

        Editor editor = FileEditorManager.getInstance(getProject()).getSelectedTextEditor();
        CanonicalFilePath currentEditorFile = editor == null ? null : CanonicalFilePathFactory.getInstance().createFor(editor, CanonicalFilePathFactory.Context.Event);
        Pair<IconStatus, String> status = computeIconStatus(currentEditorFile, null, KiteApiService.getInstance());
        updateStatusbarIcon(status);

        new ClickListener() {
            @Override
            public boolean onClick(@Nonnull MouseEvent event, int clickCount) {
                return openStatusPanel();
            }
        }.installOn(iconLabel);

        //refreshes the icon with the current status, if there's any
        KiteApiService.getInstance().addConnectionStatusListener(connectionStatusListener, this);
    }

    @Override
    public void dispose() {
        super.dispose();

        if (!disposed) {
            disposed = true;
            updateTimer.cancel();
        }
    }

    /**
     * Called when a new file is opened, the editor changed and when the last editor was closed.
     *
     * @param event The event
     */
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        refreshStatusIconAsync();
    }

    /**
     * Refreshes the icon with the current status of Kite for the currently opened file editor.
     * This method is executed on the Swing EDT.
     */
    public void refreshStatusIconAsync() {
        VirtualFile currentFile = getSelectedFile();
        refreshStatusIconAsync(currentFile != null ? CanonicalFilePathFactory.getInstance().createFor(currentFile, CanonicalFilePathFactory.Context.Event) : null);
    }

    /**
     * Refreshes the icon with the current status of Kite for the currently opened file editor.
     * This method is executed on the Swing EDT.
     *
     * @param currentFilePath The current file, if available
     */
    public void refreshStatusIconAsync(@Nullable CanonicalFilePath currentFilePath) {
        LOG.debug("refreshStatusIconAsync()");

        SwingWorkerUtil.compute(
                () -> computeIconStatus(currentFilePath, null, KiteApiService.getInstance()),
                (iconStatus) -> {
                    if (iconStatus != null) {
                        updateStatusbarIcon(iconStatus);
                    } else {
                        LOG.warn("Error while retrieving file status");
                    }
                }
        );
    }

    public boolean openStatusPanel() {
        VirtualFile currentFile = getSelectedFile();
        CanonicalFilePath currentFilePath = KiteLanguageSupport.isSupported(currentFile, KiteLanguageSupport.Feature.BasicSupport)
                ? CanonicalFilePathFactory.getInstance().createFor(currentFile, CanonicalFilePathFactory.Context.Event)
                : null;

        //trigger an update of the icon (computed in the background)
        //duplicates the isOnline check, but simplifies status logic
        refreshStatusIconAsync(currentFilePath);

        PooledThreadExecutor.INSTANCE.submit(() -> {
            try {
                KiteApiService kiteApi = KiteApiService.getInstance();
                boolean isOnline = kiteApi.checkOnlineStatus();
                LOG.debug(String.format("Detected kited's online status: %s", isOnline));

                LicenseInfo license = isOnline ? kiteApi.licenseInfo() : null;

                //small optimization: don't search for binaries if kited is online
                KiteDetector kiteDetector = KiteDetector.getInstance();
                List<Path> kiteCloudExecutables = isOnline ? Collections.emptyList() : kiteDetector.detectKiteExecutableFiles();

                KiteFileStatusResponse kiteStatusResponse = isOnline && currentFilePath != null
                        ? kiteApi.fileStatus(currentFilePath)
                        : null;

                KiteFileStatus currentFileStatus;
                if (isOnline && currentFilePath != null) {
                    currentFileStatus = kiteStatusResponse.getStatus();
                } else {
                    currentFileStatus = KiteFileStatus.Unknown;
                }

                UserInfo userInfo = isOnline
                        ? kiteApi.userInfo()
                        : null;

                boolean canInstall = KiteInstallService.getInstance().canInstall();
                boolean isInstalling = KiteInstallService.getInstance().isInstalling();

                KiteStatusModel model = new KiteStatusModel(kiteApi, isOnline, userInfo, license, WebappLinks.getInstance(),
                        currentFilePath, currentFileStatus, kiteCloudExecutables, canInstall, isInstalling, kiteStatusResponse != null ? kiteStatusResponse.getLongStatus() : null,
                        kiteStatusResponse != null ? kiteStatusResponse.getButton() : null);

                //the swing component must be made visible in the AWT EDT
                UIUtil.invokeLaterIfNeeded(() -> popupController.show(KiteStatusBarWidget.this, getProject(), model));
            } catch (Exception e) {
                LOG.error("Error while retrieving kite status", e);
            }
        });

        return false;
    }

    /**
     * Takes the values the state relies on the returns the icon status.
     *
     * @return The new icon status to use for the values passed as parameters
     */
    @Nonnull
    static Pair<IconStatus, String> computeIconStatus(@Nullable CanonicalFilePath currentFile, @Nullable Boolean connectionUnavailable, @Nonnull KiteApiService api) {
        if (KiteInstallService.getInstance().isInstalling()) {
            return new Pair<>(IconStatus.InitializingOrIndexing, null);
        }

        if (currentFile == null) {
            return new Pair<>(IconStatus.Hidden, null);
        }

        if (!KiteLanguageSupport.isSupportedFileExtension(currentFile.filenameExtension(), KiteLanguageSupport.Feature.BasicSupport)) {
            return new Pair<>(IconStatus.Hidden, null);
        }

        if (Boolean.FALSE.equals(connectionUnavailable)) {
            return new Pair<>(IconStatus.Error, null);
        }

        boolean isOnline = api.checkOnlineStatus();
        if (!isOnline) {
            return new Pair<>(IconStatus.Error, null);
        }

        KiteFileStatusResponse statusResponse = api.fileStatus(currentFile, HttpTimeoutConfig.ShortTimeout);
        KiteFileStatus fileStatus = statusResponse.getStatus();
        String statusbarText = statusResponse.getShortStatus();
        IconStatus kiteIcon;
        switch (fileStatus) {
            case Ready:
            case NoIndex: // fall-through
                kiteIcon = IconStatus.Ok;
                break;
            case Initializing: // fall-through
            case Indexing:
                kiteIcon = IconStatus.InitializingOrIndexing;
                break;
            case Unsupported:
                kiteIcon = IconStatus.Hidden;
                break;
            case Locked: // fall-through
            case Error:
                kiteIcon = IconStatus.Error;
                break;
            case Unauthorized:
                kiteIcon = IconStatus.Unknown;
                break;
            default:
                // an unknown file status could exist if there is no file open, for example
                // in this case we don't update the statusbar icon
                // because we don't know whether it's all right or kite has an error
                kiteIcon = IconStatus.Unknown;
                break;
        }
        return new Pair<>(kiteIcon, statusbarText);
    }

    private void closePopup() {
        JBPopup currentPopup = popupController.getCurrentPopup(getProject());
        if (currentPopup != null) {
            currentPopup.closeOk(null);
        }
    }

    private void updateStatusbarIcon(@Nullable Pair<IconStatus, String> iconAndText) {
        // myProject may be null if this widget is already disposed
        if (isDisposed() || getProject().isDisposed()) {
            return;
        }

        IconStatus newIconStatus = iconAndText == null ? null : iconAndText.first;
        if (newIconStatus != currentIconStatus) {
            currentIconStatus = newIconStatus;

            UIUtil.invokeLaterIfNeeded(() -> {
                iconLabel.setVisible(newIconStatus != null);
                if (newIconStatus != null) {
                    iconLabel.setBorder(JBUI.Borders.empty());
                    iconLabel.setIcon(newIconStatus.getIcon());
                    // disabled for now, as Kite's message is too long
                    //iconLabel.setText(iconAndText.second);
                } else {
                    //iconLabel.setBorder(WidgetBorder.ICON);
                }
            });
        }
    }
}

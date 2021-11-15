package com.kite.intellij.status;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.DarculaColors;
import com.intellij.ui.JBColor;
import com.intellij.util.Consumer;
import com.intellij.util.ui.UIUtil;
import com.kite.intellij.backend.KiteApiService;
import com.kite.intellij.backend.WebappLinks;
import com.kite.intellij.backend.http.HttpConnectionUnavailableException;
import com.kite.intellij.backend.http.HttpRequestFailedException;
import com.kite.intellij.backend.model.KiteFileStatus;
import com.kite.intellij.backend.model.LicenseInfo;
import com.kite.intellij.backend.model.NotificationButton;
import com.kite.intellij.backend.model.UserInfo;
import com.kite.intellij.platform.KiteDetector;
import com.kite.intellij.platform.fs.CanonicalFilePath;
import com.kite.intellij.util.KiteBrowserUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.awt.*;
import java.nio.file.Path;
import java.util.List;

/**
 * Definition of Kite engine status. This model defines all properties in the status panel.
 * It simplifies testability and the kite status component {@link KiteStatusPanelForm}.
 * <p>
 * <p>
 * This class is immutable and thread-safe.
 *
  */
@Immutable
public class KiteStatusModel {
    private static final Logger LOG = Logger.getInstance("#kite.status.model");

    private static final JBColor COLOR_STATUS_OK = new JBColor(new Color(101, 147, 234), new Color(101, 147, 234));
    private static final JBColor COLOR_STATUS_ERROR = new JBColor(new Color(253, 76, 13), DarculaColors.RED);

    private final String productLabelText;
    private final String productDetailsLabelText;
    private final String shareKiteLinkURL;
    private final WebappLinks links;
    private final boolean menuLinksEnabled;
    private final String accountActionUrl;
    private final String statusLabelText;
    private final boolean statusLabelBold;
    private final Color statusLabelColor;
    private final Color statusIconColor;
    private final boolean showDetailedStatus;
    private final String statusActionButtonText;
    private final Color statusActionButtonColor;
    private final Consumer<Project> statusActionButtonRunnable;

    public KiteStatusModel(KiteApiService kiteApi, boolean isOnline, UserInfo userInfo, @Nullable LicenseInfo licenseInfo, WebappLinks links, @Nullable CanonicalFilePath currentFile, @Nonnull KiteFileStatus fileStatus, @Nonnull List<Path> kiteCloudExecutables, boolean canInstall, boolean isInstalling, @Nullable String kiteActionLabel,
                           @Nullable NotificationButton kiteActionButton) {
        this.links = links;

        this.productLabelText = productNameLabel(licenseInfo);
        this.productDetailsLabelText = productDetailsLabelText(licenseInfo);
        this.accountActionUrl = accountActionUrl(links);

        this.statusLabelText = statusLabelText(isOnline, fileStatus, userInfo, currentFile, kiteCloudExecutables, isInstalling, kiteActionLabel);
        this.statusLabelColor = statusLabelColor(isOnline, isInstalling, fileStatus);
        this.statusLabelBold = statusLabelBold(isOnline, isInstalling);

        this.statusIconColor = statusIconColor(isOnline, kiteActionLabel);

        this.statusActionButtonText = statusButtonText(canInstall, isOnline, kiteCloudExecutables, isInstalling, kiteActionButton);
        this.statusActionButtonColor = statusButtonColor(isOnline, kiteActionButton);
        this.statusActionButtonRunnable = statusButtonActionRunnable(kiteApi, isOnline, userInfo, links, kiteCloudExecutables, kiteActionButton);

        this.showDetailedStatus = showDetailedStatus(fileStatus);

        this.shareKiteLinkURL = WebappLinks.getInstance().shareKite();

        this.menuLinksEnabled = isOnline;
    }

    public boolean isStatusLabelBold() {
        return statusLabelBold;
    }

    @Nullable
    public Color getStatusLabelColor() {
        return statusLabelColor;
    }

    public Color getStatusIconColor() {
        return statusIconColor;
    }

    public boolean isDetailedStatusVisible() {
        return showDetailedStatus;
    }

    public String getStatusLabelText() {
        return statusLabelText;
    }

    public String getHelpLinkUrl() {
        return links.helpPage();
    }

    public String getSettingsLinkUrl() {
        return links.settingsPage();
    }

    public String getSearchPythonDocsUrl() {
        return links.searchPythonDocs();
    }

    public boolean isMenuLinksEnabled() {
        return menuLinksEnabled;
    }

    public UIUtil.ComponentStyle getMenuComponentStyle() {
        return UIUtil.ComponentStyle.REGULAR;
    }

    public UIUtil.ComponentStyle getAccountActionComponentStyle() {
        return UIUtil.ComponentStyle.LARGE;
    }

    public String getAccountActionUrl() {
        return accountActionUrl;
    }

    public boolean isStatusActionButtonVisible() {
        return StringUtils.isNotEmpty(statusActionButtonText);
    }

    public String getStatusActionButtonText() {
        return statusActionButtonText;
    }

    public Color getStatusActionButtonColor() {
        return statusActionButtonColor;
    }

    public void doStatusButtonAction(Project project) {
        statusActionButtonRunnable.consume(project);
    }

    public String getProductLabelText() {
        return productLabelText;
    }

    public String getProductDetailsLabelText() {
        return productDetailsLabelText;
    }

    public String getShareKiteLinkURL() {
        return shareKiteLinkURL;
    }

    @Nullable
    private static String productNameLabel(@Nullable LicenseInfo licenseInfo) {
        if (licenseInfo == null) {
            return null;
        }
        if (licenseInfo.isKitePro()) {
            return "PRO";
        }
        return "FREE";
    }

    private static String productDetailsLabelText(@Nullable LicenseInfo licenseInfo) {
        if (licenseInfo == null) {
            return "";
        }

        if (licenseInfo.isKitePro() && !licenseInfo.isTrialing()) {
            // already bought a license
            return "";
        }

        int days = licenseInfo.getDaysRemaining();
        if (!licenseInfo.isTrialAvailable() && days > 0) {
            if (days == 1) {
                return "You have 1 day left in your Kite Pro trial";
            }
            return String.format("You have %d days left in your Kite Pro trial", days);
        }
        return "";
    }

    @Nullable
    private static String accountActionUrl(WebappLinks links) {
        return links.accountPage();
    }

    private static String statusLabelText(boolean isOnline, @Nonnull KiteFileStatus fileStatus, UserInfo userInfo, CanonicalFilePath currentFile, @Nonnull List<Path> kiteCloudExecutables, boolean isInstalling, String kiteActionLabel) {
        if (isInstalling) {
            return "<html><div style=\"text-align:right;\">Installing components<br>Kite will launch automatically when ready</div></html>";
        }

        if (kiteActionLabel != null) {
            // split the status at ".", as in the Atom plugin
            List<String> lines = StringUtil.split(kiteActionLabel, ".");
            return String.format("<html><div style=\"text-align:right;\">%s</div></html>", StringUtil.join(lines, "<br>"));
        }

        if (!isOnline) {
            //a missing plan indicates that the kite engine is not available
            if (kiteCloudExecutables.size() >= 2) {
                return "Kite engine is not running. You have multiple versions of Kite installed. Please launch your desired one";
            }

            if (kiteCloudExecutables.size() == 1) {
                return "Kite engine is not running";
            }

            return "Kite engine is not installed";
        }

        if (userInfo == null) {
            return "Kite engine is not logged in";
        }

        if (currentFile != null) {
            switch (fileStatus) {
                case Initializing:
                    return "Kite is warming up";

                case Indexing:
                    return "Kite is indexing your code...";

                case Ready:
                    return "Kite is ready and working";

                case NoIndex:
                    return "Kite is ready (no local index)";
            }
        }

        return "Open a supported file to see Kite's status";
    }

    @Nullable
    private static Color statusLabelColor(boolean isOnline, boolean installing, KiteFileStatus fileStatus) {
        if (!isOnline || installing || fileStatus == KiteFileStatus.Locked) {
            return COLOR_STATUS_ERROR;
        }

        return null;
    }

    @NotNull
    private static Color statusIconColor(boolean isOnline, String kiteActionLabel) {
        if (!isOnline || kiteActionLabel != null) {
            //a missing plan indicates that the kite engine is not available
            return COLOR_STATUS_ERROR;
        }

        return COLOR_STATUS_OK;
    }

    private static boolean statusLabelBold(boolean isOnline, boolean isInstalling) {
        return !isOnline && !isInstalling;
    }

    private static boolean showDetailedStatus(KiteFileStatus fileStatus) {
        return KiteFileStatus.Indexing.equals(fileStatus);
    }

    private static String statusButtonText(boolean canInstall, boolean isOnline, @Nonnull List<Path> kiteExecutables, boolean isInstalling, NotificationButton kiteActionButton) {
        if (kiteActionButton != null && kiteActionButton.text != null) {
            return kiteActionButton.text;
        }

        if (!canInstall) {
            return "";
        }

        if (!isInstalling && !isOnline) {
            if (kiteExecutables.size() >= 2) {
                return "";//no launch possible
            }

            if (kiteExecutables.size() == 1) {
                return "Launch now";
            }

            return "Download now";
        }

        return "";
    }

    @Nullable
    private static Consumer<Project> statusButtonActionRunnable(@Nonnull KiteApiService kiteApi,
                                                                boolean isOnline, @Nullable UserInfo userInfo,
                                                                @Nonnull WebappLinks links,
                                                                @Nonnull List<Path> kiteCloudExecutables,
                                                                @Nullable NotificationButton kiteActionButton) {
        if (kiteActionButton != null) {
            return (project) -> {
                if (kiteActionButton.isOpenAction()) {
                    KiteBrowserUtil.browse(kiteActionButton.link);
                }

                KiteStatusBarWidget.hideCurrentPopup(project);
            };
        }

        if (!isOnline) {
            if (kiteCloudExecutables.isEmpty()) {
                return (project) -> {
                    KiteBrowserUtil.browse(links.downloadPage(false));
                    KiteStatusBarWidget.hideCurrentPopup(project);
                };
            }

            //returns a process launcher
            return (project) -> launchKiteExecutable(project, first(kiteCloudExecutables), kiteApi);
        }

        if (userInfo == null) {
            return (Project project) -> {
                KiteBrowserUtil.browse(links.loginPage());

                KiteStatusBarWidget.hideCurrentPopup(project);
            };
        }

        return null;
    }

    private static void launchKiteExecutable(Project project, @Nonnull Path kiteExecutable, KiteApiService kiteApi) {
        KiteDetector kiteDetector = KiteDetector.getInstance();
        boolean success = kiteDetector.launch(kiteExecutable, false);

        if (success) {
            LOG.debug("Successfully launched kite executable: " + kiteDetector.detectKiteProcessId());

            KiteStatusBarWidget.hideCurrentPopup(project);

            Application app = ApplicationManager.getApplication();
            if (!app.isUnitTestMode()) {
                //run a status http request in the background to trigger an update of the status icon
                app.executeOnPooledThread(() -> {
                    try {
                        //retry a few times until the requests succeeded
                        for (int i = 0; i < 3; i++) {
                            Thread.sleep(500);
                            try {
                                if (kiteApi.languages() != null) {
                                    break;
                                }
                            } catch (HttpConnectionUnavailableException | HttpRequestFailedException e) {
                                // ignore
                            }
                        }
                    } catch (InterruptedException e) {
                        //ignored
                    }
                });
            }
        } else {
            Messages.showErrorDialog(project, String.format("Sorry, but Kite could not be launched. It was detected at '%s'. Please try to start it manually.", kiteExecutable.toAbsolutePath()), "Error Launching Kite");
        }
    }

    @NotNull
    private static Color statusButtonColor(boolean isOnline, NotificationButton kiteActionButton) {
        return isOnline || kiteActionButton != null && kiteActionButton.text != null ? COLOR_STATUS_OK : COLOR_STATUS_ERROR;
    }

    @Nonnull
    private static <T> T first(Iterable<T> one) {
        if (one.iterator().hasNext()) {
            return one.iterator().next();
        }

        throw new IllegalStateException("No valid object found in " + one);
    }
}

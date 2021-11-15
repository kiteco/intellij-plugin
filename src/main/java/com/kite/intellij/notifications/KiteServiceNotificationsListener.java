package com.kite.intellij.notifications;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.startup.StartupManager;
import com.kite.intellij.KiteProjectLifecycleService;
import com.kite.intellij.backend.KiteApiService;
import com.kite.intellij.backend.json.KiteJsonParsing;
import com.kite.intellij.backend.model.KiteServiceNotification;
import com.kite.intellij.ui.notifications.KiteNotifications;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Listens for HTTP responses with status 503 (service unavailable) and displays
 * a notification if the HTTP response's body contained one.
 */
public class KiteServiceNotificationsListener implements ProjectManagerListener {
    private static final Logger LOG = Logger.getInstance("#kite.notifications");

    @Override
    public void projectOpened(@NotNull Project project) {
        if (project.isDefault()) {
            return;
        }

        StartupManager.getInstance(project).runAfterOpened(() -> {
            KiteApiService.getInstance().addHttpRequestStatusListener((statusCode, e, path, requestPath) -> {
                if (statusCode == 503 && e != null) {
                    // whitelist paths for paywall notifications
                    // we must not show notifications for completions and signature panel
                    if (requestPath == null || requestPath.startsWith("/codenav/editor/related")) {
                        // show notification only for responses with a reason
                        String reason = KiteApiService.getInstance().getJsonParsing().parseReason(e.getBody());
                        if ("AllFeaturesProPaywallLocked".equals(reason)) {
                            handleKiteNotification(project, e.getBody());
                            return true;
                        }

                        return false;
                    }
                }
                return false;
            }, KiteProjectLifecycleService.getInstance(project));
        });
    }

    private void handleKiteNotification(@NotNull Project project, @Nullable String body) {
        if (project.isDisposed()) {
            return;
        }

        try {
            KiteJsonParsing jsonParsing = KiteApiService.getInstance().getJsonParsing();
            KiteServiceNotification notification = jsonParsing.parseKiteNotification(body);

            KiteNotifications.showServiceNotification(project, notification);
        } catch (Exception e) {
            LOG.warn("error parsing Kite notification", e);
        }
    }
}

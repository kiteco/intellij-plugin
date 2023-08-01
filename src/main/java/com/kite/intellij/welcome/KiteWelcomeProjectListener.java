package com.kite.intellij.welcome;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.messages.MessageBusConnection;
import com.kite.intellij.KiteConstants;
import com.kite.intellij.KiteProjectLifecycleService;
import com.kite.intellij.backend.KiteApiService;
import com.kite.intellij.backend.KiteServerSettings;
import com.kite.intellij.backend.http.KiteHttpException;
import com.kite.intellij.lang.KiteLanguage;
import com.kite.intellij.settings.KiteSettingsService;
import com.kite.intellij.startup.KiteAutostartListener;
import com.kite.intellij.util.KiteBrowserUtil;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.Collections;
import java.util.Set;

/**
 * Displays a welcome notification on first startup.
 * The notification contains a link to the public http url and an ignore action to suppress the message on next startup.
 *
  */
public class KiteWelcomeProjectListener implements ProjectActivity, DumbAware {
    private static final Logger LOG = Logger.getInstance("#kite.welcome");

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        StartupManager.getInstance(project).runAfterOpened(() -> {
            Application app = ApplicationManager.getApplication();
            MessageBusConnection connection = app.getMessageBus().connect(project.getService(KiteProjectLifecycleService.class));

            // if this plugin starts kited, then do onboarding as soon as it's available
            connection.subscribe(KiteAutostartListener.TOPIC, (KiteAutostartListener) () -> {
                JFrame frame = WindowManager.getInstance().getFrame(project);
                if (frame != null && frame.isActive()) {
                    app.executeOnPooledThread(() -> doOnboarding(project));
                }
            });

            if (KiteSettingsService.getInstance().getState().showWelcomeNotification) {
                if (app.isUnitTestMode()) {
                    doOnboarding(project);
                } else {
                    // we must not run the http request in the ui thread
                    app.executeOnPooledThread(() -> doOnboarding(project));
                }
            }
        });
        return null;
    }


    private void doOnboarding(@NotNull Project project) {
        assert ApplicationManager.getApplication().isUnitTestMode() || !ApplicationManager.getApplication().isDispatchThread();

        // don't display welcome notification or onboarding
        if (!KiteSettingsService.getInstance().getState().showWelcomeNotification) {
            return;
        }

        // this performs a HTTP request, it must be executed in the background in production mode
        Boolean hasDoneOnboarding = KiteServerSettings.HasDoneOnboarding.getBoolean(KiteApiService.getInstance());
        if (hasDoneOnboarding == null) {
            LOG.debug("Skipping onboarding due to error in retrieving has_done_onboarding");
            // default is null, e.g. when kited isn't running
            return;
        }

        Set<KiteLanguage> supportedLanguages;
        try {
            supportedLanguages = KiteApiService.getInstance().languages();
        } catch (KiteHttpException e) {
            LOG.debug("error retrieving supported languages from Kite", e);
            supportedLanguages = Collections.emptySet();
        }

        KiteLanguage onboardingLanguage = KiteOnboardingManager.getOnboardingLanguage(supportedLanguages);
        if (!hasDoneOnboarding && onboardingLanguage != null) {
            if (ApplicationManager.getApplication().isUnitTestMode()) {
                doOnboardingAction(onboardingLanguage, project);
            } else {
                ApplicationManager.getApplication().invokeLater(() -> doOnboardingAction(onboardingLanguage, project));
            }
        } else {
            Notification notification = new KiteWelcomeNotification(
                    "Welcome to the future of programming.",
                    "Kite is now integrated with your IDE.",
                    NotificationType.INFORMATION);
            notification.addAction(new ShowKiteDocsAction(notification, "Learn how to use Kite"));
            notification.addAction(new DisableWelcomeInfoAction(notification));
            notification.notify(project);
        }
    }

    private void doOnboardingAction(@Nonnull KiteLanguage language, Project project) {
        LOG.debug("Invoking KiteOnboardingAction during live onboarding. Language: " + language);

        // we have to run this on the EDT
        ApplicationManager.getApplication().assertIsDispatchThread();

        try {
            KiteOnboardingManager.openLiveOnboardingFile(project, language);

            Notification notification = new KiteWelcomeNotification("Welcome to Kite!",
                    "We've setup an interactive tutorial for you, but we have docs to get you started too!<br/>",
                    NotificationType.INFORMATION);
            notification.addAction(new ShowKiteDocsAction(notification, "Learn more about Kite"));
            notification.addAction(new DisableWelcomeInfoAction(notification));
            notification.notify(project);

            // part of the live onboarding spec, we don't want to show the live onboarding or the old-style onboarding at next startup
            disableWelcomeAtStartupNotification();
        } catch (KiteOnboardingLanguageUnsupportedError ex) {
            // ignore, showing an error for a language not supported by Kite would be confusing
        } catch (KiteOnboardingError ex) {
            KiteOnboardingManager.showErrorNotification(project, ex.getMessage());
        }
    }

    private static void disableWelcomeAtStartupNotification() {
        KiteSettingsService.getInstance().getState().showWelcomeNotification = false;
    }

    private static class ShowKiteDocsAction extends DumbAwareAction {
        private final Notification notification;

        public ShowKiteDocsAction(Notification notification, String title) {
            super(title, "Opens the help pages in your web browser", null);
            this.notification = notification;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            notification.expire();

            KiteBrowserUtil.browse(KiteConstants.KITE_HELP_URL);

            // also disable the notification at next startup
            disableWelcomeAtStartupNotification();
        }
    }

    private static class DisableWelcomeInfoAction extends DumbAwareAction {
        private final Notification notification;

        public DisableWelcomeInfoAction(Notification notification) {
            super("Don't show this at startup", "Disables this welcome notification", null);
            this.notification = notification;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            notification.expire();
            disableWelcomeAtStartupNotification();
        }
    }
}

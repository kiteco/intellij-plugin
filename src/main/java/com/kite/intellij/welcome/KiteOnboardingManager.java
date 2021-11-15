package com.kite.intellij.welcome;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PlatformUtils;
import com.kite.intellij.backend.KiteApiService;
import com.kite.intellij.backend.KiteServerSettings;
import com.kite.intellij.backend.http.KiteHttpException;
import com.kite.intellij.lang.KiteLanguage;
import com.kite.intellij.lang.KiteLanguageSupport;
import com.kite.intellij.platform.fs.CanonicalFilePath;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is a helper to simplify the testing of the onboarding logic, which is a bit complicated.
 */
public class KiteOnboardingManager {
    private static final Logger LOG = Logger.getInstance("#kite.onboarding");

    @Nullable
    private static volatile KiteLanguage testOverrideLanguage = null;

    /**
     * @param supportedLanguages The set of languages to choose from
     * @return The language to request the onboarding file from Kite. This depends on the current IDE and the enabled plugins.
     */
    @Nullable
    public static KiteLanguage getOnboardingLanguage(Set<KiteLanguage> supportedLanguages) {
        KiteLanguage override = KiteOnboardingManager.testOverrideLanguage;
        if (override != null) {
            return override;
        }

        KiteLanguage preferred = null;
        // for language-specific IDEs, prefer the main language even if other language plugins are installed
        if (PlatformUtils.isPyCharm()) {
            preferred = KiteLanguage.Python;
        } else if (PlatformUtils.isWebStorm()) {
            preferred = KiteLanguage.JavaScript;
        } else if (PlatformUtils.isGoIde()) {
            preferred = KiteLanguage.Golang;
        } else {
            // for IDEs which support multiple languages, return the first supported language of Python, JS, Go
            List<KiteLanguage> choices = Arrays.asList(KiteLanguage.Python, KiteLanguage.JavaScript, KiteLanguage.Golang);
            for (KiteLanguage choice : choices) {
                if (supportedLanguages.contains(choice) && KiteLanguageSupport.isSupportedKiteOnboardingLanguage(choice)) {
                    preferred = choice;
                    break;
                }
            }
        }

        // no onboarding if there's no language or if preferred language isn't supported
        if (preferred != null && supportedLanguages.contains(preferred)) {
            return preferred;
        }
        return null;
    }

    @TestOnly
    public static void setTestOverride(@Nullable KiteLanguage language) {
        testOverrideLanguage = language;
    }

    /**
     * Opens the onboarding file in an editor for the given language type.
     *
     * @param project  The current project
     * @param language The language, which is used to request the onboarding data from Kite
     * @return true if the language is supported and the file was successfully opened in a new editor
     * @throws KiteOnboardingError Thrown if onboarding failed
     */
    public static void openLiveOnboardingFile(@Nullable Project project, @Nonnull KiteLanguage language) throws KiteOnboardingError {
        if (project == null || project.isDisposed() || !project.isInitialized()) {
            LOG.debug("Internal error: not yet initialized");
            throw new KiteOnboardingError("Internal error: not yet initialized");
        }

        // first, check if Kite supports the language
        AtomicBoolean languageSupported = new AtomicBoolean(false);
        ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
            try {
                Set<KiteLanguage> supported = KiteApiService.getInstance().languages();
                languageSupported.set(supported.contains(language));
            } catch (KiteHttpException e) {
                // ignore, languageSupport already is false
            }
        }, "Kite Onboarding", false, project);

        if (!languageSupported.get()) {
            throw new KiteOnboardingLanguageUnsupportedError(language);
        }

        // retrieve the onboarding file in the background
        // we must not execute a HTTP request on the dispatcher thread
        AtomicReference<CanonicalFilePath> pathRef = new AtomicReference<>(null);
        ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
            LOG.debug("requesting kite onboarding file path, language: " + language);
            pathRef.set(KiteApiService.getInstance().getOnboardingFilePath(language));
        }, "Kite Onboarding", false, project);

        CanonicalFilePath path = pathRef.get();
        if (path == null) {
            LOG.debug("The path to the onboarding file could not be retrieved");
            throw new KiteOnboardingError("The path to the onboarding file could not be retrieved");
        }

        File osPath = new File(path.asOSDelimitedPath());
        if (!osPath.exists()) {
            LOG.debug("The onboarding file couldn't be found on disk");
            throw new KiteOnboardingError("The onboarding file couldn't be found on disk");
        }

        // retrieve Vfs file, refresh fs when necessary
        VirtualFile f = VfsUtil.findFileByIoFile(osPath, true);
        if (f == null || !f.isValid()) {
            LOG.debug("The onboarding file could not be located");
            throw new KiteOnboardingError("The onboarding file could not be located");
        }

        FileEditor[] opened = FileEditorManager.getInstance(project).openFile(f, true);
        if (opened.length != 1) {
            LOG.debug("The onboarding file could not be opened");
            throw new KiteOnboardingError("The onboarding file could not be opened");
        }

        // successfully opened, update the has_done_onboarding setting
        try {
            KiteServerSettings.HasDoneOnboarding.setBoolean(KiteApiService.getInstance(), true);
        } catch (KiteHttpException ignore) {
        }
    }

    public static void showErrorNotification(@Nullable Project project, @Nullable String details) {
        StringBuilder message = new StringBuilder();
        message.append("We had an internal error setting up our interactive tutorial");
        if (details != null) {
            message.append(": ").append(details);
        }
        message.append(".<br>Try again later or mail us at <em>feedback@kite.com</em>");

        new KiteWelcomeNotification(
                "We were unable to open the tutorial",
                message.toString(),
                NotificationType.ERROR, null
        ).notify(project);
    }
}

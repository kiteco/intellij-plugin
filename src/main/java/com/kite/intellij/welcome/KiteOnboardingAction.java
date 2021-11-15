package com.kite.intellij.welcome;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.kite.intellij.Icons;
import com.kite.intellij.lang.KiteLanguage;
import com.kite.intellij.lang.KiteLanguageSupport;
import org.jetbrains.annotations.NotNull;

/**
 * Run the onboarding logic.
 * Opens a file which is created by kited on demand.
 * An onboarding action is only enabled if the configured onboarding language is supported by the current IDE.
 *
  */
abstract class KiteOnboardingAction extends AnAction implements DumbAware {
    private final KiteLanguage onboardingLanguage;

    public KiteOnboardingAction(KiteLanguage onboardingLanguage) {
        super(Icons.KiteSmall);
        this.onboardingLanguage = onboardingLanguage;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        boolean isSupported = KiteLanguageSupport.isSupportedKiteOnboardingLanguage(onboardingLanguage);
        e.getPresentation().setEnabledAndVisible(isSupported);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        try {
            KiteOnboardingManager.openLiveOnboardingFile(project, onboardingLanguage);
        } catch (KiteOnboardingError ex) {
            if (project != null) {
                KiteOnboardingManager.showErrorNotification(project, ex.getMessage());
            }
        }
    }
}

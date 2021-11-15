package com.kite.intellij.editor.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.kite.intellij.lang.KiteLanguageSupport;
import com.kite.intellij.platform.KitePlatform;
import org.jetbrains.annotations.NotNull;

/**
 * Adds Kite's code completions to the available suggestions. This implementation suppresses the other available suggestions if Kite returns at least one suggestion.
 * If Kite returned none or an error occurred then the control is passed on to the other completion contributors.
 * To successfully suppress all other completions this contributor must be registered with order="FIRST" in the plugin.xml file.
 * <p>
 * The contributor will be registered only if the current platform is supported by Kite.
 *
  */
public class KiteCompletionContributor extends CompletionContributor implements DumbAware {
    public KiteCompletionContributor() {
        if (KitePlatform.isOsVersionNotSupported()) {
            return;
        }

        PsiElementPattern.Capture<PsiElement> supportedFiles = PlatformPatterns.psiElement().inVirtualFile(
                PlatformPatterns.virtualFile().with(
                        new PatternCondition<VirtualFile>("with kite extension") {
                            @Override
                            public boolean accepts(@NotNull VirtualFile virtualFile, ProcessingContext context) {
                                return KiteLanguageSupport.isSupported(virtualFile, KiteLanguageSupport.Feature.BasicSupport);
                            }
                        }
                )
        );

        extend(null, supportedFiles, new KiteCompletionProvider());
    }
}

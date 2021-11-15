package com.kite.intellij.welcome;

import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessExtension;
import com.intellij.openapi.vfs.VirtualFile;
import com.kite.intellij.lang.KiteLanguageSupport;
import org.jetbrains.annotations.NotNull;

/**
 * Grant write access to our onboarding file.
 *
 * This implementation must not contain references to language specific file types, e.g. PythonFileType.
 * This extension is always enabled and thus must not reference optional plugins.
 *
  */
public class LiveOnboardingWriteAccess implements NonProjectFileWritingAccessExtension {
    @Override
    public boolean isWritable(@NotNull VirtualFile file) {
        // kite_tuturial is used by the current version of kited
        // KiteOnboarding was used in the past
        String baseName = file.getNameWithoutExtension();
        return ("kite_tutorial".equals(baseName) || "KiteOnboarding".equalsIgnoreCase(baseName))
                && KiteLanguageSupport.isSupported(file, KiteLanguageSupport.Feature.BasicSupport);
    }
}

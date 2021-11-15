package com.kite.intellij.lang;

import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class KiteCodeFinderLanguageSupport implements LangSupportEP {
    private static final Set<String> fileExtensions = Sets.newHashSet(
            // Web languages
            "js",
            "jsx",
            "vue",
            "ts",
            "tsx",
            "css",
            "html",
            "less",
            // C style
            "c",
            "cc",
            "cpp",
            "cs",
            "h",
            "hpp",
            "m",
            // Java++
            "scala",
            "java",
            "kt",
            // Uncategorized
            "py",
            "go",
            "php",
            "rb",
            "sh"
    );

    @Override
    public boolean supportsFileExtension(@NotNull String ext) {
        return fileExtensions.contains(ext);
    }

    @Override
    public boolean supportsFeature(@NotNull KiteLanguageSupport.Feature feature) {
        return feature == KiteLanguageSupport.Feature.CodeFinder;
    }
}

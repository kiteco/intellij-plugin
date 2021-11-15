package com.kite.intellij.lang;

import com.google.common.collect.Sets;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.UnknownFileType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * This supports all file extensions, which are supported by Kite's all lexical implementation.
 * Python is excluded, because that's the only language, which has extended support in this plugin,
 * e.g. parameter info and documentation lookup.
 */
public class KiteAllLexicalLanguageSupport implements LangSupportEP {
    // without the py extension, because Python has extended support in this plugin
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
            // existing models
            "go",
            // if we have time
            "php",
            "rb",
            "sh"
    );

    @Override
    public boolean supportsFileExtension(@NotNull String ext) {
        return fileExtensions.contains(ext);
    }

    @Override
    public boolean isSupportedKiteOnboardingLanguage(@NotNull KiteLanguage language) {
        FileTypeManager mgr = FileTypeManager.getInstance();
        return language == KiteLanguage.Golang && mgr.getFileTypeByExtension("go") != UnknownFileType.INSTANCE
                || language == KiteLanguage.JavaScript && mgr.getFileTypeByExtension("js") != UnknownFileType.INSTANCE;
    }

    @Override
    public boolean supportsFeature(@NotNull KiteLanguageSupport.Feature feature) {
        return feature == KiteLanguageSupport.Feature.BasicSupport;
    }
}

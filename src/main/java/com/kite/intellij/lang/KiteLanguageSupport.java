package com.kite.intellij.lang;

import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Optional;

/**
 * {@link KiteLanguageSupport} is an application service to manage the available language support of the Kite plugin.
 * It provides the information about the supported languages.
 */
public class KiteLanguageSupport {
    public enum Feature {
        BasicSupport, CopilotDocumentation, SignatureInfo, CodeFinder
    }

    public static boolean isSupported(@Nonnull Language language, @Nonnull Feature feature) {
        LanguageFileType fileType = language.getAssociatedFileType();
        if (fileType == null) {
            return false;
        }
        return isSupportedFileExtension(fileType.getDefaultExtension(), feature);
    }

    public static boolean isSupported(@Nullable Editor editor, @Nonnull Feature feature) {
        if (editor == null) {
            return false;
        }

        return isSupported(FileDocumentManager.getInstance().getFile(editor.getDocument()), feature);
    }

    public static boolean isSupported(@Nullable PsiFile file, @Nonnull Feature feature) {
        return file != null && isSupported(file.getVirtualFile(), feature);
    }

    public static boolean isSupported(@Nullable VirtualFile file, Feature feature) {
        if (file == null) {
            return false;
        }

        String ext = file.getExtension();
        return ext != null && isSupportedFileExtension(ext, feature);
    }

    public static boolean isSupportedFileExtension(@NotNull String fileExtension, Feature feature) {
        //noinspection MissingRecentApi
        return LangSupportEP.EP.findFirstSafe(e -> e.supportsFileExtension(fileExtension) && e.supportsFeature(feature)) != null;
    }

    public static boolean isSupportedKiteOnboardingLanguage(KiteLanguage language) {
        //noinspection MissingRecentApi
        return LangSupportEP.EP.findFirstSafe(e -> e.isSupportedKiteOnboardingLanguage(language)) != null;
    }

    @NotNull
    public static String patchFilename(@NotNull String intellijSlashPath) {
        String ext = FileUtilRt.getExtension(intellijSlashPath);

        Optional<LangSupportEP> support = Arrays.stream(LangSupportEP.EP.getExtensions())
                .filter(e -> e.supportsFileExtension(ext))
                .findFirst();
        if (!support.isPresent()) {
            return intellijSlashPath;
        }

        String patched = support.get().patchFilename(intellijSlashPath);
        return patched == null ? intellijSlashPath : patched;
    }
}

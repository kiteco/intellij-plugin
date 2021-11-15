package com.kite.intellij.codeFinder;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.kite.intellij.backend.KiteApiService;
import com.kite.intellij.backend.http.HttpConnectionUnavailableException;
import com.kite.intellij.backend.http.HttpRequestFailedException;
import com.kite.intellij.backend.http.HttpTimeoutConfig;
import com.kite.intellij.platform.fs.CanonicalFilePath;
import com.kite.intellij.platform.fs.CanonicalFilePathFactory;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class KiteCodeFinderManager {
    public static void requestRelatedCode(@NotNull VirtualFile virtualFile, @Nullable Integer lineNo) throws KiteFindRelatedError {
        try {
            CanonicalFilePath canonical = CanonicalFilePathFactory.getInstance().createFor(virtualFile, CanonicalFilePathFactory.Context.CodeFinder);
            if (canonical == null) {
                throw new KiteFindRelatedError("Code Finder does not support the `." + virtualFile.getExtension() + "` file extension yet.");
            }
            KiteApiService.getInstance().relatedCode(canonical, lineNo, HttpTimeoutConfig.DefaultTimeout);
        } catch (HttpConnectionUnavailableException | HttpRequestFailedException ignore) {
        }
    }

    public static void showErrorNotification(@Nullable Project project, @Nullable String details) {
        new KiteCodeFinderNotification(
                "Kite Code Finder Error",
                details,
                NotificationType.ERROR
        ).notify(project);
    }
}

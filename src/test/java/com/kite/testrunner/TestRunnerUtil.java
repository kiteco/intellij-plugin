package com.kite.testrunner;

import com.google.common.util.concurrent.Atomics;
import com.intellij.ide.IdeEventQueue;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.util.ThrowableRunnable;
import com.kite.intellij.action.signatureInfo.KiteSignaturePopupManager;
import com.kite.intellij.editor.events.TestcaseEditorEventListener;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.ide.PooledThreadExecutor;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicReference;

public class TestRunnerUtil {
    private TestRunnerUtil() {
    }

    @Nonnull
    public static String resolvePlaceholders(@Nonnull String value, TestContext context) {
        AtomicReference<String> result = Atomics.newReference(value);

        context.getContextProperties().forEach((key, v) ->
                result.set(StringUtils.replace(result.get(), "${" + key + "}", v))
        );

        return result.get();
    }

    public static String relativePathCurrentFile(TestContext context) {
        // IntelliJ's test runner puts files into the temp-fs directory /src
        VirtualFile rootDir = context.getFixture().getFile().getVirtualFile().getFileSystem().findFileByPath("/src");

        return VfsUtilCore.getRelativePath(context.getFixture().getFile().getVirtualFile(), rootDir, '/');
    }

    public static void flushEvents(TestContext context) throws Throwable {
        Application app = ApplicationManager.getApplication();
        if (app.isDispatchThread()) {
            PsiDocumentManager.getInstance(context.getProject()).commitAllDocuments();
        } else {
            EdtTestUtil.runInEdtAndWait((ThrowableRunnable<Throwable>) () -> PsiDocumentManager.getInstance(context.getProject()).commitAllDocuments());
        }

        // flush Swing EDT
        app.invokeAndWait(() -> IdeEventQueue.getInstance().flushQueue(), app.getAnyModalityState());

        // flush application pool
        PooledThreadExecutor.INSTANCE.submit(() -> {
        }).get();

        TestcaseEditorEventListener.sleepForQueueWork(context.getProject());

        Editor editor = FileEditorManager.getInstance(context.getProject()).getSelectedTextEditor();
        if (editor != null) {
            app.invokeAndWait(() -> KiteSignaturePopupManager.flushEvents(editor), app.getAnyModalityState());
        }
    }
}

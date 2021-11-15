package com.kite.intellij.backend;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.kite.intellij.KiteConstants;
import com.kite.intellij.lang.KiteLanguageSupport;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * This is an application service, which keeps track of the current Kite server settings.
 */
public class KiteServerSettingsServiceImpl implements Disposable, KiteServerSettingsService {
    @Nonnull
    private volatile Long maxFileSizeBytes = KiteConstants.MAX_FILE_SIZE_BYTES_FALLBACK;

    public KiteServerSettingsServiceImpl() {
        // update the setting as soon as Kite is available
        KiteApiService.getInstance().addConnectionStatusListener((connectionAvailable, error) -> {
            if (connectionAvailable) {
                updateMaxFileSize();
            } else {
                maxFileSizeBytes = KiteConstants.MAX_FILE_SIZE_BYTES_FALLBACK;
            }
        }, this);

        // update the setting when new files are activated
        // updates are only requested for supported files, i.e. files with supported extensions
        // updates are also only requested when kite was detected as online to avoid unnecessary HTTP request, which
        // would timeout anyway
        ApplicationManager.getApplication().getMessageBus().connect(this).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                if (KiteLanguageSupport.isSupported(event.getNewFile(), KiteLanguageSupport.Feature.BasicSupport)) {
                    updateMaxFileSize();
                }
            }
        });

        // read the value for the first when this service is created
        updateMaxFileSize();
    }

    // resets the stored max file size, useful in tests
    @Override
    public void reset() {
        this.maxFileSizeBytes = KiteConstants.MAX_FILE_SIZE_BYTES_FALLBACK;
    }

    @Override
    public void dispose() {
        // the connection status listener is already registered with the Disposer
    }

    @Override
    public long getMaxFileSizeBytes() {
        return this.maxFileSizeBytes;
    }

    protected void updateMaxFileSize() {
        Runnable action = () -> {
            Long kitedMaxFileSize = KiteServerSettings.MaxFileSize.getLong(KiteApiService.getInstance());
            if (kitedMaxFileSize != null) {
                maxFileSizeBytes = kitedMaxFileSize * 1024L;
            } else {
                // revert to the fallback value
                maxFileSizeBytes = KiteConstants.MAX_FILE_SIZE_BYTES_FALLBACK;
            }
        };

        // always update in the background in non-test mode, don't block the EDT
        if (ApplicationManager.getApplication().isUnitTestMode()) {
            action.run();
        } else {
            ApplicationManager.getApplication().executeOnPooledThread(action);
        }
    }
}

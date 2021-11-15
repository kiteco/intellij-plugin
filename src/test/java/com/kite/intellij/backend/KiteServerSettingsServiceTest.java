package com.kite.intellij.backend;

import com.kite.intellij.KiteConstants;
import com.kite.intellij.backend.http.test.MockKiteHttpConnection;
import com.kite.intellij.test.KiteLightFixtureTest;

import java.util.concurrent.atomic.AtomicInteger;

public class KiteServerSettingsServiceTest extends KiteLightFixtureTest {
    @Override
    public void setUp() throws Exception {
        super.setUp();

        MockKiteServerSettingsService.getInstance().setEnabled(true);
    }

    @Override
    public void tearDown() throws Exception {
        MockKiteServerSettingsService.getInstance().setEnabled(true);

        super.tearDown();
    }

    public void testMaxFileSetting() {
        assertEquals(KiteConstants.MAX_FILE_SIZE_BYTES_FALLBACK, KiteServerSettingsService.getInstance().getMaxFileSizeBytes());

        AtomicInteger maxFileSizeResponse = new AtomicInteger(2048);
        MockKiteHttpConnection.getInstance().addGetPathHandler("/clientapi/settings/max_file_size_kb",
                (path, queryParams) -> maxFileSizeResponse.toString(), getTestRootDisposable());
        MockKiteApiService.getInstance().enableHttpCalls();

        // force a refresh in the service
        MockKiteApiService.getInstance().turnOffline();
        MockKiteApiService.getInstance().turnOnline();
        assertEquals(2048 * 1024, KiteServerSettingsService.getInstance().getMaxFileSizeBytes());

        // fallback must be active when kite is offline
        MockKiteApiService.getInstance().turnOffline();
        assertEquals(KiteConstants.MAX_FILE_SIZE_BYTES_FALLBACK, KiteServerSettingsService.getInstance().getMaxFileSizeBytes());

        maxFileSizeResponse.set(4096);
        MockKiteApiService.getInstance().turnOnline();
        assertEquals(4096 * 1024, KiteServerSettingsService.getInstance().getMaxFileSizeBytes());
    }
}
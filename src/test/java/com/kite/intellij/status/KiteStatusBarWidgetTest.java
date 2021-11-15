package com.kite.intellij.status;

import com.kite.intellij.backend.MockKiteApiService;
import com.kite.intellij.backend.http.HttpStatusException;
import com.kite.intellij.backend.http.test.MockKiteHttpConnection;
import com.kite.intellij.platform.MockKiteInstallService;
import com.kite.intellij.platform.fs.UnixCanonicalPath;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

public class KiteStatusBarWidgetTest extends KiteLightFixtureTest {
    @Test
    public void testInstalling() {
        MockKiteApiService.getInstance().turnOffline();
        MockKiteInstallService.getInstance().setInstalling(true);

        Assert.assertEquals("The icon status must be 'Indexing' when Kite is installing.",
                IconStatus.InitializingOrIndexing,
                KiteStatusBarWidget.computeIconStatus(null, Boolean.TRUE, getKiteApiService()).first);
    }

    // this covers "not installed", and "not running"
    @Test
    public void testConnectionUnavailable() {
        MockKiteApiService.getInstance().turnOffline();

        Assert.assertEquals("The icon status must be 'Hidden' when the connection is broken but no file is opened.",
                IconStatus.Hidden,
                KiteStatusBarWidget.computeIconStatus(null, Boolean.TRUE, getKiteApiService()).first);

        Assert.assertEquals("The icon status must be 'error' when the connection is broken and an supported file is open",
                IconStatus.Error,
                KiteStatusBarWidget.computeIconStatus(new UnixCanonicalPath("/home/user/test.py"), Boolean.TRUE, getKiteApiService()).first);
    }

    @Test
    public void testNoFile() {
        MockKiteApiService.getInstance().turnOffline();
        IconStatus status = KiteStatusBarWidget.computeIconStatus(null, null, getKiteApiService()).first;
        Assert.assertEquals("The icon status must be 'no file' for unsupported files.", IconStatus.Hidden, status);

        MockKiteApiService.getInstance().turnOnline();
        status = KiteStatusBarWidget.computeIconStatus(null, null, getKiteApiService()).first;
        Assert.assertEquals("The icon status must be 'no file' for unsupported files.", IconStatus.Hidden, status);
    }

    @Test
    public void testUnsupportedFile() {
        MockKiteApiService.getInstance().turnOffline();
        IconStatus status = KiteStatusBarWidget.computeIconStatus(new UnixCanonicalPath("/home/user/test.txt"), null, getKiteApiService()).first;
        Assert.assertEquals("The icon status must be 'hidden' for unsupported files.", IconStatus.Hidden, status);

        MockKiteApiService.getInstance().turnOnline();
        status = KiteStatusBarWidget.computeIconStatus(new UnixCanonicalPath("/home/user/test.txt"), null, getKiteApiService()).first;
        Assert.assertEquals("The icon status must be 'hidden' for unsupported files.", IconStatus.Hidden, status);
    }

    @Test
    public void testSupportedFileNotRunning() {
        MockKiteApiService.getInstance().turnOffline();

        IconStatus status = KiteStatusBarWidget.computeIconStatus(new UnixCanonicalPath("/home/user/test.py"), null, getKiteApiService()).first;
        Assert.assertEquals("The icon status must be 'error' while Kite isn't running.", IconStatus.Error, status);
    }

    @Test
    public void testSupportedFileUnauthorized() {
        MockKiteApiService.getInstance().turnOnline();
        MockKiteApiService.getInstance().enableHttpCalls();
        MockKiteHttpConnection.getInstance().addGetPathHandler("/clientapi/status", (path, queryParams) -> {
            throw new HttpStatusException("unauthorzed", HttpStatus.SC_UNAUTHORIZED, null);
        }, getTestRootDisposable());

        IconStatus status = KiteStatusBarWidget.computeIconStatus(new UnixCanonicalPath("/home/user/test.py"), null, getKiteApiService()).first;
        Assert.assertEquals("The icon status must be 'error' when no user is logged in", IconStatus.Unknown, status);
    }

    @Test
    public void testSupportedFileNoIndex() {
        assertFileStatus("noIndex", "The icon status must be 'syncing/indexing' for a syncing file", IconStatus.Ok);
    }

    @Test
    public void testSupportedFileInitializing() {
        assertFileStatus("initializing", "The icon status must be 'initializing/indexing' for a file whose model is initializing", IconStatus.InitializingOrIndexing);
    }

    @Test
    public void testSupportedFileIndexing() {
        assertFileStatus("indexing", "The icon status must be 'syncing/indexing' for an indexed file", IconStatus.InitializingOrIndexing);
    }

    @Test
    public void testSupportedFileReady() {
        assertFileStatus("ready", "The icon status must be 'ok' for a ready file", IconStatus.Ok);
    }

    @Test
    public void testSupportedFileRunning() {
        MockKiteApiService.getInstance().turnOnline();

        IconStatus status = KiteStatusBarWidget.computeIconStatus(new UnixCanonicalPath("/home/user/test.py"), null, getKiteApiService()).first;
        Assert.assertEquals("The icon status must be 'grey' for a supported file.", IconStatus.Ok, status);
    }

    private void assertFileStatus(String jsonStatus, String message, IconStatus expectedStatus) {
        MockKiteApiService.getInstance().turnOnline();
        MockKiteApiService.getInstance().enableHttpCalls();
        MockKiteHttpConnection.getInstance().addGetPathHandler("/clientapi/status", (path, queryParams) -> jsonStatus(jsonStatus), getTestRootDisposable());

        IconStatus status = KiteStatusBarWidget.computeIconStatus(new UnixCanonicalPath("/home/user/test.py"), null, getKiteApiService()).first;
        Assert.assertEquals(message, expectedStatus, status);
    }

    @NotNull
    private static String jsonStatus(String status) {
        return "{\"status\": \"" + status + "\"}";
    }
}
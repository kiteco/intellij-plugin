package com.kite.intellij.test;

import com.intellij.ide.IdeEventQueue;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationsManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.CompletionAutoPopupTester;
import com.kite.intellij.KiteRuntimeInfo;
import com.kite.intellij.action.KiteActionOverrideLifecycleListener;
import com.kite.intellij.action.MockKiteDelegatingAction;
import com.kite.intellij.action.signatureInfo.MockKiteSignaturePopupManager;
import com.kite.intellij.backend.KiteServerSettingsService;
import com.kite.intellij.backend.MockKiteApiService;
import com.kite.intellij.backend.http.test.MockKiteHttpConnection;
import com.kite.intellij.editor.events.TestcaseEditorEventListener;
import com.kite.intellij.platform.MockKiteDetector;
import com.kite.intellij.platform.MockKiteInstallService;
import com.kite.intellij.settings.KiteSettings;
import com.kite.intellij.settings.KiteSettingsService;
import com.kite.intellij.ui.KiteTestUtil;
import com.kite.intellij.ui.notifications.KiteNotification;
import com.kite.intellij.util.KiteBrowserUtil;
import fi.iki.elonen.NanoHTTPD;
import org.junit.Assert;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class KiteLightFixtureTest extends BasePlatformTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();

        new KiteActionOverrideLifecycleListener().overrideActions();

        //turn off to avoid output to stdout of socket closed exceptions
        Logger.getLogger(NanoHTTPD.class.getName()).setLevel(Level.OFF);

        //reset any http call history
        MockKiteHttpConnection.getInstance().resetHistory();

        //Drain the queue
        TestcaseEditorEventListener.sleepForQueueWork(getProject());

        //reset the kite api (which receives queue work)
        MockKiteApiService api = getKiteApiService();
        api.clearTestData();
        api.disableHttpCalls();
        api.disableHttpStatusListeners();

        //reset the stored kited server settings
        KiteServerSettingsService.getInstance().reset();

        //reset the browser util
        KiteBrowserUtil.reset();

        //default to non-retina test environment
        KiteRuntimeInfo.setHighDpiScreen(false);

        //reset the popup manager call count
        MockKiteSignaturePopupManager.reset();

        //reset the application settings back to the default state (no persistance across tests)
        KiteSettingsService.getInstance().loadState(new KiteSettings());

        //reset the list of launched commands
        MockKiteDetector.getInstance().reset();

        //reset install service
        MockKiteInstallService.getInstance().reset();

        //close our active notifications
        Notification[] notifications = NotificationsManager.getNotificationsManager().getNotificationsOfType(Notification.class, getProject());
        for (Notification notification : notifications) {
            if (notification instanceof KiteNotification) {
                notification.expire();
            }
        }

        //reset the fallback state of our mock actions
        ((MockKiteDelegatingAction) ActionManager.getInstance().getAction(KiteActionOverrideLifecycleListener.INTELLIJ_PARAMETER_INFO_ACTION_ID)).resetTestData();

        //reset the settings
        KiteSettingsService.getInstance().getState().showWelcomeNotification = true;

        KiteTestUtil.isIntegrationTesting = false;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        ApplicationManager.getApplication().invokeAndWait(() -> IdeEventQueue.getInstance().flushQueue(), ModalityState.defaultModalityState());
    }

    @Override
    protected String getTestDataPath() {
        String relativeTestDataPath = getBasePath();
        if (SystemInfo.isWindows) {
            relativeTestDataPath = relativeTestDataPath.replace('/', File.separatorChar);
        }

        return KiteTestUtils.getTestDataRoot() + (relativeTestDataPath.startsWith(File.separator) ? relativeTestDataPath : (File.separator + relativeTestDataPath));
    }

    public MockKiteApiService getKiteApiService() {
        return MockKiteApiService.getInstance();
    }

    protected void configurePythonFileByTestName() {
        myFixture.configureByFile(getTestDataPath() + File.separator + getTestName(true) + ".py");
    }

    /**
     * IntelliJ suppresses focus event listeners in unit testing mode (see FileEditorManagerImpl.java below the comment // Transfer focus into editor)
     * We emulate the focus event to let the events be the same as in production mode.
     *
     * @param filename
     * @param fileContent The files content
     */
    protected PsiFile configureByFileContentAndFocus(String filename, String fileContent) {
        PsiFile psiFile = myFixture.configureByText(filename, fileContent);
        Assert.assertNotNull(psiFile);

        KiteTestUtils.emulateFocusEvent(psiFile);

        return psiFile;
    }

    protected String loadFile(String pathInTestCasePath) {
        String root = getTestDataPath();
        File file = new File(root, pathInTestCasePath);
        if (!file.exists()) {
            throw new RuntimeException("File not found  " + file.getAbsolutePath());
        }

        try {
            return StreamUtil.readTextFrom(new FileReader(file));
        } catch (IOException e) {
            throw new RuntimeException("Error loding file " + pathInTestCasePath);
        }
    }

    protected void runWithAutocompletionEnabled(Consumer<CompletionAutoPopupTester> runnable) throws Throwable {
        CompletionAutoPopupTester tester = new CompletionAutoPopupTester(myFixture);
        tester.runWithAutoPopupEnabled(() -> runnable.accept(tester));
    }
}

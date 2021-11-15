package com.kite.intellij.status;

import com.google.common.collect.Lists;
import com.intellij.testFramework.TestDataFile;
import com.kite.intellij.backend.MockKiteApiService;
import com.kite.intellij.backend.WebappLinks;
import com.kite.intellij.backend.http.HttpConnectionUnavailableException;
import com.kite.intellij.backend.http.test.MockKiteHttpConnection;
import com.kite.intellij.backend.model.KiteFileStatus;
import com.kite.intellij.backend.model.UserInfo;
import com.kite.intellij.platform.MockKiteDetector;
import com.kite.intellij.platform.fs.CanonicalFilePath;
import com.kite.intellij.platform.fs.UnixCanonicalPath;
import com.kite.intellij.test.KiteLightFixtureTest;
import com.kite.intellij.test.KiteTestUtils;
import com.kite.intellij.util.KiteBrowserUtil;
import org.jetbrains.annotations.NonNls;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class KiteStatusModelTest extends KiteLightFixtureTest {
    private static final UserInfo DEFAULT_TEST_USER = new UserInfo("userId", "user", "user@kite.local.com", "bio", true, false, false);
    @NonNls
    private static final String COLOR_BLUE = "com.intellij.ui.JBColor[r=101,g=147,b=234]";

    @Test
    public void testStaticMenu() {
        MockKiteApiService api = setupApi("proPlan.json");
        KiteStatusModel model = setupModel(api, KiteFileStatus.Ready);

        Assert.assertEquals("http://localhost:46624/clientapi/desktoplogin?d=%2Fpython%2Fdocs", model.getSearchPythonDocsUrl());
        Assert.assertEquals("kite://settings", model.getSettingsLinkUrl());
        Assert.assertEquals("https://help.kite.com/category/45-intellij-pycharm-integration", model.getHelpLinkUrl());
    }

    @Test
    public void testKiteRunning() {
        MockKiteApiService api = setupApi("proPlan.json");
        KiteStatusModel model = setupModel(api, KiteFileStatus.Ready);

        Assert.assertEquals("The status text must indicate that kite is running", "Kite is ready and working", model.getStatusLabelText());
        Assert.assertNull("The status text must must not be colored if everything is all right", model.getStatusLabelColor());
        Assert.assertEquals("The status icon must indicate that kite is running", COLOR_BLUE, model.getStatusIconColor().toString());
        Assert.assertFalse("No bold status label if everything is all right", model.isStatusLabelBold());

        Assert.assertFalse("The detailed status must not be visible if kite is available", model.isDetailedStatusVisible());

        Assert.assertFalse("Must not be visible for 'kite is running'", model.isDetailedStatusVisible());

        Assert.assertFalse("The action button must not be set", model.isStatusActionButtonVisible());

    }

    @Test
    public void testKitePro() {
        MockKiteApiService api = setupApi("proPlan.json");
        KiteStatusModel model = setupModel(api, KiteFileStatus.Ready);

        Assert.assertEquals("The account url should point to the remote account settings", "http://localhost:46624/clientapi/desktoplogin?d=%2Fsettings%2Faccount", model.getAccountActionUrl());
    }

    @Test
    public void testKiteInitializing() {
        MockKiteApiService api = setupApi("proPlan.json");
        KiteStatusModel model = setupModel(api, KiteFileStatus.Initializing);

        Assert.assertEquals("Kite is warming up", model.getStatusLabelText());
        Assert.assertNull("The status label color must indicate normal state", model.getStatusLabelColor());
        Assert.assertEquals("The status icon color must indicate normal state", COLOR_BLUE, model.getStatusIconColor().toString());
        Assert.assertFalse("The detailed status must not be visible for model warmup", model.isDetailedStatusVisible());
        Assert.assertFalse("Indexing shows normal state, i.e. non-bold status", model.isStatusLabelBold());
    }

    @Test
    public void testKiteIndexing() {
        MockKiteApiService api = setupApi("proPlan.json");
        KiteStatusModel model = setupModel(api, KiteFileStatus.Indexing);

        Assert.assertEquals("Kite is indexing your code...", model.getStatusLabelText());
        Assert.assertNull("The status label color must indicate normal state", model.getStatusLabelColor());
        Assert.assertEquals("The status icon color must indicate normal state", COLOR_BLUE, model.getStatusIconColor().toString());

        Assert.assertTrue("The detailed status must be visible if kite is indexing", model.isDetailedStatusVisible());

        Assert.assertFalse("Indexing shows normal state, i.e. non-bold status", model.isStatusLabelBold());
    }

    @Test
    public void testKiteNoIndex() {
        MockKiteApiService api = setupApi("proPlan.json");
        KiteStatusModel model = setupModel(api, KiteFileStatus.NoIndex);

        Assert.assertEquals("Kite is ready (no local index)", model.getStatusLabelText());
        Assert.assertFalse("The detailed status must not be visible if kite is ready", model.isDetailedStatusVisible());
        Assert.assertNull("The status label color must indicate normal state", model.getStatusLabelColor());
        Assert.assertEquals("The status icon color must indicate normal state", COLOR_BLUE, model.getStatusIconColor().toString());

        Assert.assertFalse("noIndex shows normal state, i.e. non-bold status", model.isStatusLabelBold());
    }

    @Test
    public void testKiteOfflineCloud() {
        MockKiteApiService api = setupApi(null);

        List<Path> kiteCloudExecutables = Lists.newArrayList(new Path[]{Paths.get(getTestDataPath(), "kite.exe.txt")});

        KiteStatusModel model = setupModel(api, false, KiteFileStatus.Unknown, null, kiteCloudExecutables, Collections.emptyList());

        Assert.assertEquals("The status text must indicate that kite is offline", "Kite engine is not running", model.getStatusLabelText());
        Assert.assertNotNull(model.getStatusLabelColor());
        Assert.assertEquals("The status icon must indicate that kite is offline", "com.intellij.ui.JBColor[r=253,g=76,b=13]", model.getStatusLabelColor().toString());
        Assert.assertEquals("The status icon color must indicate that kite is offline", "com.intellij.ui.JBColor[r=253,g=76,b=13]", model.getStatusIconColor().toString());

        Assert.assertTrue("Bold status label if kite is not running and working", model.isStatusLabelBold());

        Assert.assertFalse("The detailed status must be hidden if kite is offline", model.isDetailedStatusVisible());

        Assert.assertTrue("The status action must be visible if Kite is not running", model.isStatusActionButtonVisible());
        Assert.assertEquals("Launch button must be shown", "Launch now", model.getStatusActionButtonText());
        Assert.assertEquals("The action color must indicate non-normal state", "com.intellij.ui.JBColor[r=253,g=76,b=13]", model.getStatusActionButtonColor().toString());

        Assert.assertTrue("The launched commands must be empty", MockKiteDetector.getInstance().getLaunchedCommands().isEmpty());
        model.doStatusButtonAction(getProject());
        Assert.assertEquals("The launched commands must have one command", 1, MockKiteDetector.getInstance().getLaunchedCommands().size());
        Assert.assertEquals("The cloud command must have been launched", kiteCloudExecutables.get(0).toString(), MockKiteDetector.getInstance().getLaunchedCommands().get(0));

        Assert.assertFalse("Menu items are still enabled when kited is online, but logged-out", model.isMenuLinksEnabled());
    }

    @Test
    public void testKiteUninstalled() {
        MockKiteApiService api = setupApi(null);

        KiteStatusModel model = setupModel(api, false, KiteFileStatus.Unknown, null, Collections.emptyList(), Collections.emptyList());

        Assert.assertEquals("The status text must indicate that kite is not installed", "Kite engine is not installed", model.getStatusLabelText());
        Assert.assertNotNull(model.getStatusLabelColor());
        Assert.assertEquals("The status icon must indicate that kite is not installed", "com.intellij.ui.JBColor[r=253,g=76,b=13]", model.getStatusLabelColor().toString());
        Assert.assertEquals("The status icon must indicate that kite is not installed", "com.intellij.ui.JBColor[r=253,g=76,b=13]", model.getStatusIconColor().toString());

        Assert.assertTrue("Bold status label if kite is not running and working", model.isStatusLabelBold());

        Assert.assertFalse("The detailed status must be hidden if kite is not running", model.isDetailedStatusVisible());

        Assert.assertFalse("Must not be visible if kite is not available", model.isDetailedStatusVisible());

        Assert.assertTrue("The status action must be visible if Kite is not running", model.isStatusActionButtonVisible());
        Assert.assertEquals("Launch button must be shown", "Download now", model.getStatusActionButtonText());
        Assert.assertEquals("The action color must indicate non-normal state", "com.intellij.ui.JBColor[r=253,g=76,b=13]", model.getStatusActionButtonColor().toString());

        model.doStatusButtonAction(getProject());
        Assert.assertEquals("The download page must have been opened by the action", WebappLinks.getInstance().downloadPage(false), KiteBrowserUtil.openedUrls.get(KiteBrowserUtil.openedUrls.size() - 1));
    }

    @Test
    public void testKiteMultipleCloudVersions() {
        MockKiteApiService api = setupApi(null);

        KiteStatusModel model = setupModel(api, false, KiteFileStatus.Unknown, null, Lists.newArrayList(Paths.get("one/kited.exe"), Paths.get("two/kited.exe")), Collections.emptyList());

        Assert.assertEquals("The status text must indicate that more than one version was found.", "Kite engine is not running. You have multiple versions of Kite installed. Please launch your desired one", model.getStatusLabelText());
        Assert.assertNotNull(model.getStatusLabelColor());
        Assert.assertEquals("The status icon must indicate that kite is not installed", "com.intellij.ui.JBColor[r=253,g=76,b=13]", model.getStatusLabelColor().toString());
        Assert.assertEquals("The status icon must indicate that kite is not installed", "com.intellij.ui.JBColor[r=253,g=76,b=13]", model.getStatusIconColor().toString());

        Assert.assertFalse("The 1st launch button must be hidden", model.isStatusActionButtonVisible());
    }

    @Test
    public void testKiteNotLoggedIn() {
        MockKiteApiService api = setupApi("communityPlanTrialAvailable.json");

        KiteStatusModel model = setupModel(api, true, KiteFileStatus.Unknown, null, Lists.newArrayList(), Collections.emptyList());

        Assert.assertEquals("The status text must indicate that kite it not logged in", "Kite engine is not logged in", model.getStatusLabelText());
        Assert.assertNull("The status text color must indicate that kite is offline", model.getStatusLabelColor());
        Assert.assertEquals("The status icon must indicate that kite is offline", "com.intellij.ui.JBColor[r=101,g=147,b=234]", model.getStatusIconColor().toString());

        Assert.assertFalse("Not a bold status label if kite is not logged in", model.isStatusLabelBold());

        Assert.assertFalse("The detailed status must be hidden if kite is offline", model.isDetailedStatusVisible());

        Assert.assertFalse("Must not be visible if kite is not available", model.isDetailedStatusVisible());

        Assert.assertFalse(model.isStatusActionButtonVisible());
        Assert.assertEquals("Login button must not be shown", "", model.getStatusActionButtonText());

        Assert.assertTrue("Menu items are still enabled when kited is online, but logged-out", model.isMenuLinksEnabled());
    }

    @Test
    public void testKiteNoEditedFile() {
        MockKiteApiService api = setupApi("proPlan.json");
        KiteStatusModel model = setupModel(api, KiteFileStatus.Unknown, DEFAULT_TEST_USER, null);

        Assert.assertEquals("If not file is edited kite's indexing status is unavailable.", "Open a supported file to see Kite's status", model.getStatusLabelText());

        Assert.assertNull("No status color if no file is edited", model.getStatusLabelColor());
        Assert.assertFalse("The status label must have normal font weight if not file is edited", model.isStatusLabelBold());
        Assert.assertEquals("Icon must have normal color indication if not file is edited", COLOR_BLUE, model.getStatusIconColor().toString());
    }

    @Test
    public void testIndexingVersusLogout() {
        MockKiteApiService api = setupApi("communityPlanNoTrialAvailable.json");
        KiteStatusModel model = setupModel(api, true, KiteFileStatus.Indexing, null, Collections.emptyList(), Collections.emptyList());

        Assert.assertEquals("The status text must not show the indexing state, but the missing login", "Kite engine is not logged in", model.getStatusLabelText());
        Assert.assertEquals("The status action must not offer the login", "", model.getStatusActionButtonText());

        model.doStatusButtonAction(getProject());
        Assert.assertEquals("The login link must have been opened", 1, KiteBrowserUtil.openedUrls.size());
        Assert.assertEquals("The login link must have been opened", "kite://settings", KiteBrowserUtil.openedUrls.get(0));
    }

    @Override
    protected String getBasePath() {
        return "intellij/status/";
    }

    private KiteStatusModel setupModel(MockKiteApiService api, KiteFileStatus kiteFileStatus) {
        return setupModel(api, true, kiteFileStatus, DEFAULT_TEST_USER, Collections.emptyList(), Collections.emptyList());
    }

    private KiteStatusModel setupModel(MockKiteApiService api, boolean isOnline, KiteFileStatus kiteFileStatus, UserInfo userInfo, List<Path> kiteCloudExecutables, List<Path> kiteEnterpriseExecutables) {
        return setupModel(api, isOnline, kiteFileStatus, userInfo, new UnixCanonicalPath("/home/user/projectDir/test.py"), kiteCloudExecutables);
    }

    private KiteStatusModel setupModel(MockKiteApiService api, KiteFileStatus kiteFileStatus, UserInfo userInfo, CanonicalFilePath currentFile) {
        return setupModel(api, true, kiteFileStatus, userInfo, currentFile, Collections.emptyList());
    }

    private KiteStatusModel setupModel(MockKiteApiService api, boolean isOnline, KiteFileStatus kiteFileStatus, UserInfo userInfo, CanonicalFilePath currentFile, List<Path> kiteCloudExecutables) {
        return new KiteStatusModel(api, isOnline, userInfo, api.licenseInfo(), WebappLinks.getInstance(),
                currentFile, kiteFileStatus, kiteCloudExecutables, true, false, null, null);
    }

    private MockKiteApiService setupApi(@TestDataFile String planJsonFile) {
        MockKiteApiService api = getKiteApiService();
        api.enableHttpCalls();

        //fixme
        MockKiteHttpConnection http = MockKiteHttpConnection.getInstance();
        http.addPostPathHandler("/redirect/pro", (path, body) -> "", getTestRootDisposable());
        http.addPostPathHandler("/redirect/trial", (path, body) -> "", getTestRootDisposable());
        http.addPostPathHandler("/api/account/resendVerification", (path, body) -> "", getTestRootDisposable());

        try {
            if (planJsonFile != null) {
                //fixme
                http.addGetPathHandler("/clientapi/plan", (path, queryParams) -> {
                    try {
                        return KiteTestUtils.loadTestDataFile(getBasePath() + "/" + planJsonFile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, getTestRootDisposable());
            } else {
                http.addGetPathHandler("/clientapi/plan", (path, queryParams) -> {
                    throw new HttpConnectionUnavailableException("kite is offline");
                }, getTestRootDisposable());
            }

            return api;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

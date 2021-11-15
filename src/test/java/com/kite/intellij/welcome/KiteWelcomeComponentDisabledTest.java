package com.kite.intellij.welcome;

import com.google.gson.GsonBuilder;
import com.intellij.notification.NotificationsManager;
import com.kite.intellij.backend.http.HttpStatusException;
import com.kite.intellij.backend.http.test.MockKiteHttpConnection;
import com.kite.intellij.settings.KiteSettingsService;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This test runs two methods. The first one makes sure the component notification has been shown. The next run method
 * makes sure it doesn't appear again if it was disabled with the settings.
 */
public class KiteWelcomeComponentDisabledTest extends KiteLightFixtureTest {
    /**
     * The spec defines that all onboarding must be skipped when the settings request returns an error
     * the default is a 404 response for tests
     */
    @Test
    public void testLiveOnboardingSettingsError() {
        expireVisibleNotifications();

        KiteWelcomeProjectListener welcome = new KiteWelcomeProjectListener();
        Assert.assertEquals("No notifications expected", 0, visibleNotifications().length);
        KiteSettingsService.getInstance().getState().showWelcomeNotification = true;

        welcome.projectOpened(getProject());
        KiteWelcomeNotification[] notifications = visibleNotifications();
        Assert.assertEquals("No notifications expected when kited returns an error for /clientapi/settings/has_done_onboarding", 0, notifications.length);

        expireVisibleNotifications();
    }

    @Test
    public void testNewNotification() {
        // the spec defines that the old onboarding must only be done when has_done_onboarding doesn't return an error,
        // therefore we return a 'true' instead of a 404 reponse
        MockKiteHttpConnection.getInstance().addGetPathHandler("/clientapi/settings/has_done_onboarding", (path, queryParams) -> "true", getTestRootDisposable());

        expireVisibleNotifications();

        KiteWelcomeProjectListener welcome = new KiteWelcomeProjectListener();
        Assert.assertEquals("No notifications expected", 0, visibleNotifications().length);
        KiteSettingsService.getInstance().getState().showWelcomeNotification = true;

        welcome.projectOpened(getProject());
        KiteWelcomeNotification[] notifications = visibleNotifications();
        Assert.assertEquals("One notification expected", 1, notifications.length);
        Assert.assertEquals("New notification have actions", 2, notifications[0].getActions().size());

        expireVisibleNotifications();
    }

    @Test
    public void testLiveOnboarding() {
        // enable the live onboarding
        MockKiteHttpConnection http = MockKiteHttpConnection.getInstance();
        http.addGetPathHandler("/clientapi/settings/has_done_onboarding", (path, queryParams) -> "false", getTestRootDisposable());
        http.addGetPathHandler("/clientapi/plugins/onboarding_file", (path, queryParams) -> {
            try {
                File f = File.createTempFile("KiteOnboarding", ".py");
                return new GsonBuilder().create().toJson(f.getAbsolutePath());
            } catch (IOException e) {
                throw new HttpStatusException("Error creating temp file KiteOnboarding.py", HttpStatus.SC_INTERNAL_SERVER_ERROR, null);
            }
        }, getTestRootDisposable());

        expireVisibleNotifications();

        KiteWelcomeProjectListener welcome = new KiteWelcomeProjectListener();
        Assert.assertEquals("No notifications expected", 0, visibleNotifications().length);
        KiteSettingsService.getInstance().getState().showWelcomeNotification = true;

        welcome.projectOpened(getProject());
        KiteWelcomeNotification[] notifications = visibleNotifications();
        Assert.assertEquals("One notification expected", 1, notifications.length);
        Assert.assertEquals("Expected welcome notification", "Welcome to Kite!", notifications[0].getTitle());
        Assert.assertEquals("New notification have actions", 2, notifications[0].getActions().size());

        // make sure that the request to update "has_done_onboarding" has been called
        List<String> history = http.getHttpRequestStringHistory();
        Assert.assertTrue("kited setting must be be updated after live onboarding: " + history, history.contains("POST /clientapi/settings/has_done_onboarding\ntrue"));

        expireVisibleNotifications();
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }

    private void expireVisibleNotifications() {
        for (KiteWelcomeNotification notification : visibleNotifications()) {
            notification.expire();
        }
    }

    @Nonnull
    private KiteWelcomeNotification[] visibleNotifications() {
        return NotificationsManager.getNotificationsManager().getNotificationsOfType(KiteWelcomeNotification.class, getProject());
    }
}
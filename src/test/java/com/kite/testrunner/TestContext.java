package com.kite.testrunner;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.util.messages.MessageBusConnection;
import com.kite.intellij.ui.KiteTestUtil;
import com.kite.testrunner.model.NotificationInfo;
import com.kite.testrunner.model.TestStep;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A TestContext is passed into every action and expectation and contains the needed data and the information about the
 * current test execution.
 * It provides the notifications which were shown in the currently executed test.
 *
  */
public class TestContext {
    private final Path dataPath;
    private final CodeInsightTestFixture fixture;
    private final Path rootPath;
    private final Gson gson;
    private final List<NotificationInfo> notifications;
    private final MessageBusConnection msgBusConnection;
    private final Path testFilePath;
    private final Map<String, String> contextProperties = Maps.newConcurrentMap();
    private final List<String> whitelist = Lists.newCopyOnWriteArrayList();
    private final List<String> blacklist = Lists.newCopyOnWriteArrayList();
    private final List<String> ignoredFiles = Lists.newCopyOnWriteArrayList();
    private final boolean integrationTest;
    private int stepIndex;
    private TestStep step;
    private Disposable testRootDisposable;

    public TestContext(Path testFilePath, Path dataPath, CodeInsightTestFixture fixture, Gson gson) {
        this.testFilePath = testFilePath;
        this.dataPath = dataPath;
        this.rootPath = dataPath.getParent();
        this.fixture = fixture;
        this.gson = gson;

        this.integrationTest = KiteTestUtil.isIntegrationTesting();

        this.notifications = Collections.synchronizedList(Lists.newArrayList());
        this.msgBusConnection = getProject().getMessageBus().connect(getProject());

        putContextProperty("plugin", "intellij");

        initListeners();
    }

    public boolean isIntegrationTest() {
        return integrationTest;
    }

    public Map<String, String> getContextProperties() {
        return contextProperties;
    }

    public void putContextProperty(String name, String value) {
        contextProperties.put(name, value);
    }

    public TestStep getStep() {
        return step;
    }

    public void setStep(TestStep step) {
        this.step = step;
    }

    public void update() {
        msgBusConnection.deliverImmediately();
    }

    public Gson getGson() {
        return gson;
    }

    public Path getDataPath() {
        return dataPath;
    }

    public Path getRootPath() {
        return rootPath;
    }

    public CodeInsightTestFixture getFixture() {
        return fixture;
    }

    public Project getProject() {
        return fixture.getProject();
    }

    public List<NotificationInfo> getOpenNotifications() {
        return notifications;
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public void setStepIndex(int stepIndex) {
        this.stepIndex = stepIndex;
    }

    public Path getTestFilePath() {
        return testFilePath;
    }

    public List<String> getWhitelist() {
        return whitelist;
    }

    public List<String> getBlacklist() {
        return blacklist;
    }

    public List<String> getIgnoredFiles() {
        return ignoredFiles;
    }

    public Disposable getTestRootDisposable() {
        return testRootDisposable;
    }

    public void setTestRootDisposable(Disposable testRootDisposable) {
        this.testRootDisposable = testRootDisposable;
    }

    public void updateWhitelist(List<String> whitelist) {
        this.whitelist.clear();
        this.whitelist.addAll(whitelist);
    }

    public void updateBlacklist(List<String> blacklist) {
        this.blacklist.clear();
        this.blacklist.addAll(blacklist);
    }

    public void updateIgnoredFiles(List<String> ignored) {
        this.ignoredFiles.clear();
        this.ignoredFiles.addAll(ignored);
    }

    private void initListeners() {
        msgBusConnection.subscribe(Notifications.TOPIC, new Notifications() {
            @Override
            public void notify(@Nonnull Notification notification) {
                NotificationInfo info = new NotificationInfo("warning", notification.getTitle(), notification.getContent());
                notifications.add(info);

                notification.whenExpired(() -> notifications.remove(info));
            }
        });
    }
}


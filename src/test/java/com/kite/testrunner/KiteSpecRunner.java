package com.kite.testrunner;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.util.ThrowableRunnable;
import com.kite.intellij.backend.MockKiteApiService;
import com.kite.intellij.backend.http.HttpStatusException;
import com.kite.intellij.backend.http.test.MockKiteHttpConnection;
import com.kite.intellij.http.GetRequestHandler;
import com.kite.intellij.http.RequestWithBodyHandler;
import com.kite.intellij.settings.KiteSettings;
import com.kite.intellij.settings.KiteSettingsService;
import com.kite.testrunner.actions.*;
import com.kite.testrunner.expectations.*;
import com.kite.testrunner.model.TestRoute;
import com.kite.testrunner.model.TestSpec;
import com.kite.testrunner.model.TestStep;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Executes a single test specification.
 *
  */
public class KiteSpecRunner {
    private static final Logger LOG = Logger.getInstance("#kite.test");

    private final Path jsonSpecPath;
    private final Path dataDir;
    private final Path relativeSpecPath;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, TestAction> actions;
    private final Map<String, TestExpectation> expectations;

    public KiteSpecRunner(Path jsonSpecPath, Path dataDir) {
        assert Files.exists(jsonSpecPath);
        assert Files.exists(dataDir);

        this.jsonSpecPath = jsonSpecPath;
        this.dataDir = dataDir;
        this.relativeSpecPath = dataDir.getParent().relativize(jsonSpecPath);

        this.actions = Maps.newHashMap();
        Lists.newArrayList(
                new OpenFileAction(),
                new NewFileAction(),
                new MoveCursorAction(),
                new InputTextAction(),
                new RemoveTextAction(),
                new RequestHoverAction(),
                new RequestCompletionAction(),
                new UpdateBlacklistAction(),
                new UpdateRouteAction()
        ).forEach(a -> actions.put(a.getId(), a));

        this.expectations = Maps.newHashMap();
        Lists.newArrayList(
                new RequestExpectation(),
                new RequestCountExpectation(),
                new NotificationExpectation(),
                new NotificationCountExpectation(),
                new HoverDataExpectation()
        ).forEach(e -> expectations.put(e.getId(), e));
    }

    /**
     * Exceutes the test.
     */
    public void run(CodeInsightTestFixture fixture) throws Throwable {
        TestContext context = new TestContext(relativeSpecPath, dataDir, fixture, gson);
        context.setTestRootDisposable(fixture.getTestRootDisposable());

        TestSpec spec;
        try {
            spec = gson.fromJson(Files.newBufferedReader(jsonSpecPath), TestSpec.class);
        } catch (IOException e) {
            throw new TestFailedException(context, "JSON parsing failed", e);
        }

        if (spec.ignore) {
            LOG.info("Test disabled in JSON file. Ignroing.");
            return;
        }

        // return early if the test isn't meant to run within the live environment
        if (context.isIntegrationTest() && Boolean.FALSE.equals(spec.live_environment)) {
            return;
        }

        context.updateWhitelist(Arrays.asList(spec.setup.whitelist));
        context.updateBlacklist(Arrays.asList(spec.setup.blacklist));
        context.updateIgnoredFiles(Arrays.asList(spec.setup.ignored));

        setup(context, spec);
        LOG.info(String.format("Executing test \"%s\" (%s)", relativeSpecPath.toString(), spec.description));

        TestStep[] steps = spec.test;
        for (int i = 1; i <= steps.length; i++) {
            TestStep step = steps[i - 1];
            context.setStepIndex(i);
            context.setStep(step);

            LOG.info(String.format("\tRunning step %d / %d (%s)", i, steps.length, step.description));

            try {
                switch (step.step) {
                    case "action":
                        doAction(context, step);
                        break;
                    case "expect":
                        doExpectation(context, step);
                        break;
                    case "expect_not":
                        doNotExpectation(context, step);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected step type: " + step.step);
                }
            } catch (ConcurrentModificationException | InterruptedException | ExecutionException e) {
                throw new TestFailedException(context, "Step failed", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void setup(TestContext context, TestSpec spec) {
        MockKiteApiService.getInstance().enableHttpCalls();
        MockKiteApiService.getInstance().enableHttpStatusListeners();

        KiteSettings settings = KiteSettingsService.getInstance().getState();
        settings.useNewCompletions = spec.setup.completion_snippets;

        MockKiteHttpConnection mockHTTP = MockKiteHttpConnection.getInstance();
        for (TestRoute route : spec.setup.routes) {
            route.applyTo(context, mockHTTP);
        }

        // setup mocked kited HTTP endpoints
        // GET
        mockHTTP.addGetPathHandler("/clientapi/permissions/notify", handlePermissionsNotify(context), context.getTestRootDisposable());
        mockHTTP.addGetPathHandler("/clientapi/projectdir", handleProjectdir(), context.getTestRootDisposable());

        // POST
        mockHTTP.addPostPathHandler("/clientapi/editor/event", handleEditorEvent(context), context.getTestRootDisposable());
    }

    private RequestWithBodyHandler handleCounter(TestContext context) {
        return (path, payload) -> "ok";
    }

    @NotNull
    private GetRequestHandler handleProjectdir() {
        return (path, queryParams) -> {
            if (!queryParams.containsKey("filename")) {
                throw new HttpStatusException("filename not found", HttpStatus.SC_BAD_REQUEST, null);
            }
            return "/";
        };
    }

    @NotNull
    private GetRequestHandler handlePermissionsNotify(TestContext context) {
        return (path, queryParams) -> {
            //IntelliJ test files are in /src
            String filename = toProjectPath(queryParams.get("filename"));
            for (String p : context.getBlacklist()) {
                if (filename.startsWith(p)) {
                    throw new HttpStatusException("File is blacklisted: ", HttpStatus.SC_FORBIDDEN, null);
                }
            }

            for (String p : context.getIgnoredFiles()) {
                if (filename.startsWith(p)) {
                    throw new HttpStatusException("File is ignored: ", HttpStatus.SC_FORBIDDEN, null);
                }
            }

            // not blacklisted and not ignored
            return "";
        };
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private RequestWithBodyHandler handleEditorEvent(TestContext context) {
        return (path, payload) -> {
            Map<String, Object> body = context.getGson().fromJson(payload, Map.class);
            String filename = toProjectPath((String) body.get("filename"));

            for (String p : context.getIgnoredFiles()) {
                if (filename.startsWith(p)) {
                    throw new HttpStatusException("File is ignored: " + p, HttpStatus.SC_FORBIDDEN, null);
                }
            }

            for (String p : context.getWhitelist()) {
                if (filename.startsWith(p)) {
                    return ""; //whitelisted
                }
            }

            throw new HttpStatusException("File is not whitelisted: " + filename, HttpStatus.SC_FORBIDDEN, null);
        };
    }

    @NotNull
    private static String toProjectPath(String filename) throws HttpStatusException {
        if (filename == null) {
            throw new HttpStatusException("Filename not found", HttpStatus.SC_INTERNAL_SERVER_ERROR, null);
        }

        filename = filename.replace(File.separatorChar, '/');
        if (!filename.startsWith("/src")) {
            throw new HttpStatusException("Filename not expected test dir /src", HttpStatus.SC_INTERNAL_SERVER_ERROR, null);
        }

        return filename.substring("/src/".length());
    }

    private void doAction(TestContext context, TestStep step) throws Throwable {
        TestAction action = actions.get(step.type);
        if (action == null) {
            throw new IllegalStateException("Unknown action type: " + step.type);
        }

        if (action.runInEventDispatchThread()) {
            EdtTestUtil.runInEdtAndWait((ThrowableRunnable<Throwable>) () -> action.run(context));
        } else {
            try {
                action.run(context);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    private void doExpectation(TestContext context, TestStep step) throws Throwable {
        TestExpectation expectation = expectations.get(step.type);
        if (expectation == null) {
            throw new IllegalStateException("Unknown expectation type: " + step.type);
        }

        EdtTestUtil.runInEdtAndWait((ThrowableRunnable<Throwable>) () -> ApplicationManager.getApplication().runWriteAction(PsiDocumentManager.getInstance(context.getProject())::commitAllDocuments));
        TestRunnerUtil.flushEvents(context);

        // update content after queue and pools were flushed
        context.update();

        if (expectation instanceof RetryableExpectation) {
            TestFailedException last = null;
            for (int i = 0; i < 5; i++) {
                try {
                    expectation.run(context);
                    last = null;
                    break;
                } catch (TestFailedException e) {
                    last = e;
                    LOG.debug("Retryable exception failed, attempt " + i + ". Sleeping 500ms.");
                    Thread.sleep(500);
                }

            }
            if (last != null) {
                throw last;
            }
        } else {
            expectation.run(context);
        }
    }

    private void doNotExpectation(TestContext context, TestStep step) throws Throwable {
        try {
            doExpectation(context, step);
        } catch (TestFailedException e) {
            //expectation failed --> okay
            return;
        }

        throw new TestFailedException(context, "expect_not failed");
    }
}

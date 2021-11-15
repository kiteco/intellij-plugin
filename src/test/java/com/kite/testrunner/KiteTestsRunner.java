package com.kite.testrunner;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.application.impl.ApplicationInfoImpl;
import com.kite.intellij.settings.KiteSettingsService;
import com.kite.intellij.test.KiteLightFixtureTest;
import com.kite.intellij.test.KiteTestUtils;
import com.kite.intellij.ui.KiteTestUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Runs alls JSON test specs as a JUnit test.
 *
  */
@RunWith(Parameterized.class)
public class KiteTestsRunner extends KiteLightFixtureTest {
    private final Path specFile;

    public KiteTestsRunner(String specFile) {
        this.specFile = Paths.get(specFile);
    }

    @Parameterized.Parameters(name = "Spec {0}")
    public static Iterable<String> data() throws IOException {
        List<String> result = Lists.newArrayList();
        result.addAll(locateTestSpecs(Paths.get("tests")));
        return result;
    }

    @Before
    public void beforeTest() throws Exception {
        // the superclass is a JUnit 3 Test, we need to run its init in this JUnit4 test
        // ApplicationInfoEx.getInstanceEx() isn't available yet at this point
        // 2020.3+ defines, that setUp() must not be called
        try {
            if (ApplicationInfoImpl.getShadowInstance().getBuild().getBaselineVersion() < 203) {
                super.setUp();
            }
        } catch (Exception e) {
            // 2020.1 throws an exception when getShadowInstance() is accessed before the setup
            super.setUp();
        }

        KiteSettingsService.getInstance().getState().useNewCompletions = false;

        KiteTestUtil.isIntegrationTesting = true;
    }

    @After
    public void afterTest() throws Exception {
        KiteTestUtil.isIntegrationTesting = false;

        // the superclass is a JUnit 3 Test, we need to run its shutdown in this JUnit4 test
        // ApplicationInfoEx.getInstanceEx() isn't available yet at this point
        // 2020.3+ defines, that tearDown() must not be called
        try {
            if (ApplicationInfoImpl.getShadowInstance().getBuild().getBaselineVersion() < 203) {
                super.tearDown();
            }
        } catch (Exception e) {
            // 2020.1 throws an exception when getShadowInstance() is accessed before the setup
            super.tearDown();
        }
    }

    @Test
    public void testAllSpecs() throws Throwable {
        Path specRoot = testSpecRoot();
        Path dataDir = specRoot.resolve("data");
        Path specFilePath = specRoot.resolve(specFile);

        // run the test, but not in the event dispatcher thread
        new KiteSpecRunner(specFilePath, dataDir).run(myFixture);
    }

    @NotNull
    static Path testSpecRoot() {
        return Paths.get(KiteTestUtils.getTestDataRoot()).getParent().resolve("test-spec");
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }

    @Override
    protected boolean runInDispatchThread() {
        return false;
    }

    /**
     * Returns a list of relative paths of JSON spec files to test.
     */
    protected static List<String> locateTestSpecs(Path testSubdir) throws IOException {
        Path root = testSpecRoot();
        Path absoluteSubdir = root.resolve(testSubdir);
        if (Files.notExists(absoluteSubdir)) {
            return Collections.emptyList();
        }

        Path dirSpecFile = absoluteSubdir.resolve("pycharm.json");
        if (Files.notExists(dirSpecFile)) {
            dirSpecFile = absoluteSubdir.resolve("default.json");
        }

        // fall back to "all dirs" if no default.json file is present to be able to execute tests in older versions of the test-spec repo
        Stream<Path> subdirsStream;
        if (Files.exists(dirSpecFile)) {
            Gson gson = new GsonBuilder().create();
            String[] dirs = gson.fromJson(Files.newBufferedReader(dirSpecFile), String[].class);
            subdirsStream = Arrays.stream(dirs).map(absoluteSubdir::resolve);
        } else {
            subdirsStream = Files.list(absoluteSubdir).filter(Files::isDirectory);
        }

        return subdirsStream.flatMap(path -> {
            try {
                return Files.list(path);
            } catch (IOException e) {
                throw new RuntimeException("error listing files of " + path);
            }
        })
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith(".json"))
                .sorted()
                .map(root::relativize)
                .map(Path::toString)
                .collect(Collectors.toList());
    }
}

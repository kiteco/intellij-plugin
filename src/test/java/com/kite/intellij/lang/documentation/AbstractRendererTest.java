package com.kite.intellij.lang.documentation;

import com.kite.intellij.test.KiteLightFixtureTest;
import com.kite.intellij.test.KiteTestUtils;
import org.junit.Assert;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.function.Function;

/**
 */
public abstract class AbstractRendererTest extends KiteLightFixtureTest {
    protected void compareFiles(@Nonnull String jsonFilePath, @Nonnull String expectedHtmlFilePath, Function<String, String> renderFunction) throws IOException {
        String basePath = getBasePath().endsWith(File.separator) ? getBasePath() : getBasePath() + File.separatorChar;
        String json = KiteTestUtils.loadTestDataFile(basePath + jsonFilePath);
        String expected = KiteTestUtils.loadTestDataFile(basePath + expectedHtmlFilePath);

        String renderedHtml = renderFunction.apply(json);

        String prettyRenderedHtml = KiteTestUtils.prettyPrintHtmlBody(renderedHtml);
        String prettyExpected = KiteTestUtils.prettyPrintHtmlBody(expected);

        if (KiteTestUtils.isOverridingReferenceRenderings()) {
            KiteTestUtils.saveTestDataFile(basePath + expectedHtmlFilePath, prettyRenderedHtml);
            return;
        }

        Assert.assertEquals("The rendered html must be equal to the expected data.", prettyExpected, prettyRenderedHtml);
    }

    @Override
    protected String getBasePath() {
        return "python/documentation/detailedRenderer/";
    }
}

package com.kite.intellij.lang.documentation;

import com.kite.intellij.backend.json.KiteJsonParsing;
import com.kite.intellij.lang.documentation.linkHandler.SignatureLinkData;
import com.kite.intellij.test.KiteTestUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.OptionalInt;

/**
 * Checks the rendering of signature info data.
 *
  * @see com.kite.intellij.backend.model.Calls
 */
public class PebbleSignatureRendererTest extends AbstractRendererTest {
    /**
     * If the user has a community plan then the popular patterns will not be displayed but a placeholder with upgrade links will be displayed.
     */
    @Test
    public void testPrintCommunity() throws Exception {
        assertRendering("printCommunity");
    }

    @Test
    public void testFuncNameFallback() throws Exception {
        assertRendering("funcNameFallback");
    }

    /**
     * If the user has a pro plan then the popuplar patterns will be shown.
     */
    @Test
    public void testPrintPro() throws Exception {
        assertRendering("printPro");
    }

    @Test
    public void testPrintProCollapsedPatterns() throws Exception {
        assertRendering("printProCollapsedPatterns", false, 0, false, false);
    }

    @Test
    public void testPlotCommunityVarArg() throws Exception {
        assertRendering("plotCommunityVarArg", true, 0, false, false);
    }

    @Test
    public void testPlotCommunityKwArgs() throws Exception {
        assertRendering("plotCommunityKwArgs", true, 1, true, false);
    }

    @Test
    public void testAcorrCommunity() throws Exception {
        assertRendering("acorrCommunity");
    }

    @Test
    public void testStaticPro() throws Exception {
        assertRendering("staticPro");
    }

    @Test
    public void testLocalFunction() throws Exception {
        assertRendering("localFunction");
    }

    @Test
    public void testJsonDumpsArg9() throws Exception {
        assertRendering("jsonDumpsArg9", true, 9, false, false);
    }

    @Test
    public void testJsonDumpsArg9Expanded() throws Exception {
        assertRendering("jsonDumpsArg9Expanded", true, 9, true, true);
    }

    @Test
    public void testJsonDumpsKwargs() throws Exception {
        assertRendering("jsonDumpsKwargs", true, 0, true, true);
    }

    @Test
    public void testJsonDumpsKwargsCollapsed() throws Exception {
        assertRendering("jsonDumpsKwargsCollapsed", true, 0, true, false);
    }

    @Test
    public void testJsonDumpsKwargsInactive() throws Exception {
        assertRendering("jsonDumpsKwargsInactive", true, 0, false, true);
    }

    @Test
    public void testKwargIndex() throws Exception {
        assertRendering("kwargIndex", true, 0, true, true);
    }

    @Test
    public void testConstructor() throws Exception {
        assertRendering("constructor", true, 0, true, true);
    }

    @Test
    public void testNumpyAmax() throws Exception {
        assertRendering("numpyAmax", true, 0, true, true);
    }

    private String renderSignature(String json, String planJson, int argIndex, boolean highlightKwarg, boolean expandKwargs, boolean expandPopularPatterns) {
        KiteJsonParsing jsonParsing = new KiteJsonParsing();
        PebbleDocumentationRenderer renderer = new PebbleDocumentationRenderer();

        return renderer.render(jsonParsing.parseCalls(json), new SignatureLinkData(OptionalInt.of(argIndex), highlightKwarg, expandKwargs, expandPopularPatterns, false), OptionalInt.empty());
    }

    private void assertRendering(String functionName) throws IOException {
        assertRendering(functionName, true, 0, false, false);
    }

    private void assertRendering(String functionName, boolean expandPopularPatterns, int argIndex, boolean inKwargs, boolean expandKwarg) throws IOException {
        String planJson = KiteTestUtils.loadTestDataFile(getBasePath() + "/signature/" + functionName + "/plan.json");
        compareFiles("signature/" + functionName + "/data.json", "signature/" + functionName + "/expected.html", json -> renderSignature(json, planJson, argIndex, inKwargs, expandKwarg, expandPopularPatterns));
    }
}
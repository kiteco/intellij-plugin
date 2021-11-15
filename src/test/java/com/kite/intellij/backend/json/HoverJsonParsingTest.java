package com.kite.intellij.backend.json;

import com.kite.intellij.backend.model.*;
import com.kite.intellij.backend.response.HoverResponse;
import com.kite.intellij.test.KiteTestUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 */
public class HoverJsonParsingTest {
    @Test
    public void testPythonModuleHover() throws Exception {
        HoverResponse hover = new KiteJsonParsing().parseHoverResponse(KiteTestUtils.loadTestDataFile("model/json/hover/module_python.json"));
        assertModuleBasics(hover);

        ModuleDetails module = (ModuleDetails) hover.getFirstSymbol().getFirstValue().getDetail();
        Assert.assertEquals(1, module.getMembers().length);
    }

    @Test
    public void testPythonFunctionHover() throws Exception {
        HoverResponse hover = new KiteJsonParsing().parseHoverResponse(KiteTestUtils.loadTestDataFile("model/json/hover/function_python.json"));

        Detail detail = hover.getFirstValue().getDetail();
        Assert.assertTrue(detail instanceof PythonFunctionDetails);

        PythonFunctionDetails pythonDetails = (PythonFunctionDetails) detail;

        Parameter[] parameters = ((FunctionDetails) detail).getParameters();
        Assert.assertTrue(parameters[0] instanceof PythonParameter);

        PythonParameter first = (PythonParameter) parameters[0];
        Assert.assertNotNull("python default value", first.getDefaultValue().getFirst());
        Assert.assertEquals("python default value", first.getDefaultValue().getFirst().getRepresentation());

        Assert.assertTrue(pythonDetails.hasKwarg());
        Assert.assertNotNull(pythonDetails.getKwarg());

        Assert.assertEquals(1, pythonDetails.getKwargParameters().length);
    }

    @Test
    public void testPythonFunctionNullDetailsHover() throws Exception {
        //this threw an exception
        new KiteJsonParsing().parseHoverResponse(KiteTestUtils.loadTestDataFile("model/json/hover/python/function_nullDetails.json"));
    }

    @Test
    public void testPythonFunctionNonNullDetailsHover() throws Exception {
        //this threw an exception
        new KiteJsonParsing().parseHoverResponse(KiteTestUtils.loadTestDataFile("model/json/hover/python/function_nonNullDetails.json"));
    }

    protected void assertModuleBasics(HoverResponse hover) {
        Assert.assertNotNull(hover);

        Assert.assertEquals("name", hover.getPartOfSyntax());

        SymbolExt firstSymbol = hover.getFirstSymbol();
        Assert.assertNotNull(firstSymbol);

        Assert.assertEquals("", firstSymbol.getId().getValue());
        Assert.assertEquals("requests", firstSymbol.getName());
        Assert.assertEquals("symbol `requests`", firstSymbol.getSynopsis());

        Assert.assertNotNull(firstSymbol.getValues());
        Assert.assertNotNull(firstSymbol.getValues());
        Assert.assertEquals(1, firstSymbol.getValues().getValues().length);

        Detail detail = firstSymbol.getValues().getValues()[0].getDetail();
        Assert.assertNotNull(detail);
        Assert.assertTrue(detail instanceof ModuleDetails);
        Assert.assertEquals(DetailType.Module, detail.getType());
        Assert.assertEquals(10, ((ModuleDetails) detail).getTotalMembers());
        Assert.assertEquals(1, ((ModuleDetails) detail).getMembers().length);
        Assert.assertEquals("requests.models.Response", ((ModuleDetails) detail).getMembers()[0].getId().getValue());
        Assert.assertEquals("Response", ((ModuleDetails) detail).getMembers()[0].getName());

        Assert.assertEquals("html report", hover.getReport().getDescriptionHtml());
        Assert.assertEquals("text report", hover.getReport().getDescriptionText());

        //check usages
        Assert.assertEquals(0, hover.getReport().getTotalUsages());
        Assert.assertEquals(0, hover.getReport().getUsages().length);
    }
}
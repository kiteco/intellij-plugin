package com.kite.intellij.backend.json;

import com.kite.intellij.backend.model.*;
import com.kite.intellij.test.KiteTestUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class FunctionDetailsJsonParsingTest {
    @Test
    public void testPythonFunctionDetails() throws Exception {
        Detail detail = new KiteJsonParsing().fromJson(KiteTestUtils.loadTestDataFile("model/json/details/functionDetails_python.json"), FunctionDetails.class);
        Assert.assertTrue(detail instanceof PythonFunctionDetails);

        PythonFunctionDetails function = (PythonFunctionDetails) detail;
        Assert.assertEquals(3, function.getParameters().length);
        Assert.assertTrue(function.isParametersAvailable());

        Parameter first = function.getParameters()[0];
        Assert.assertEquals("a", first.getName());
        Assert.assertEquals("1", first.getInferredValue().getValues()[0].getRepresentation());
        Assert.assertEquals(Kind.Instance, first.getInferredValue().getValues()[0].getKind());
        Assert.assertEquals("int", first.getInferredValue().getValues()[0].getType());
        Assert.assertEquals("__builtin__.int", first.getInferredValue().getValues()[0].getTypeId());

        Parameter second = function.getParameters()[1];
        Assert.assertEquals("b", second.getName());

        Parameter third = function.getParameters()[2];
        Assert.assertEquals("c", third.getName());

        Assert.assertTrue(function.hasKwarg());
        Assert.assertEquals(2, function.getKwargParameters().length);

        Assert.assertEquals(5, function.getSignatures().length);
        Assert.assertEquals("url", function.getSignatures()[0].getArgs()[0].getName());
    }
}
package com.kite.intellij.backend;

import com.google.common.collect.Lists;
import com.kite.intellij.backend.json.KiteJsonParsing;
import com.kite.intellij.backend.model.Call;
import com.kite.intellij.backend.model.Calls;
import com.kite.intellij.backend.model.PythonSignature;
import com.kite.intellij.backend.model.Signature;
import com.kite.intellij.test.KiteLightFixtureTest;
import com.kite.intellij.test.KiteTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class SignatureJsonParsingTest extends KiteLightFixtureTest {
    @Test
    public void testPythonSignature() throws Exception {
        Calls calls = new KiteJsonParsing().parseCalls(KiteTestUtils.loadTestDataFile("model/json/signature/signature_python.json"));
        Assert.assertNotNull(calls);

        Assert.assertEquals(1, calls.size());
        Assert.assertFalse(calls.isEmpty());

        Call first = calls.getCalls()[0];
        Assert.assertEquals("plot", first.getFuncName());
        Assert.assertEquals("matplotlib.pyplot.plot", first.getCallee().getId().getValue());

        Assert.assertEquals(1, first.getSignatures().length);

        Signature firstSignature = first.getSignatures()[0];
        Assert.assertTrue(firstSignature instanceof PythonSignature);

        PythonSignature pythonSignature = (PythonSignature) firstSignature;

        Assert.assertEquals("x", pythonSignature.getArgs()[0].getName());
        Assert.assertEquals(3, pythonSignature.getArgs()[0].getTypes().length);
        Assert.assertEquals("__builtin__.list", pythonSignature.getArgs()[0].getTypes()[0].getId().getValue());
        Assert.assertEquals("list", pythonSignature.getArgs()[0].getTypes()[0].getName());
        Assert.assertEquals(3, pythonSignature.getArgs()[0].getTypes()[0].getExamples().size());
        Assert.assertEquals(Lists.newArrayList("[]", "[0, 1]", "[0,1]"), pythonSignature.getArgs()[0].getTypes()[0].getExamples());

        Assert.assertTrue(pythonSignature.getKwargs().length == 1);
        Assert.assertEquals("x", pythonSignature.getKwargs()[0].getName());
    }

}

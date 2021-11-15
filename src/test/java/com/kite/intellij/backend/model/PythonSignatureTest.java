package com.kite.intellij.backend.model;

import org.junit.Assert;
import org.junit.Test;

public class PythonSignatureTest {
    @Test
    public void testEmptyCombined() throws Exception {
        PythonSignature signature = new PythonSignature(new SignatureBase(null), null);
        Assert.assertEquals(0, signature.getArgs().length);
        Assert.assertEquals(0, signature.getKwargs().length);
        Assert.assertEquals(0, signature.getCombinedArgs().length);
    }

    @Test
    public void testEquals() throws Exception {
        PythonSignature a = new PythonSignature(new SignatureBase(ParameterExample.EMPTY_ARRAY), ParameterExample.EMPTY_ARRAY);
        PythonSignature b = new PythonSignature(new SignatureBase(ParameterExample.EMPTY_ARRAY), ParameterExample.EMPTY_ARRAY);
        PythonSignature c = new PythonSignature(new SignatureBase(ParameterExample.EMPTY_ARRAY), new ParameterExample[]{new ParameterExample("arg1", true, ParameterTypeExample.EMPTY_ARRAY)});

        Assert.assertEquals(a, a);
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);
        Assert.assertEquals(c, c);

        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(b, c);
    }

    @Test
    public void testSimpleCombined() throws Exception {
        ParameterExample arg1 = new ParameterExample("arg1", false, ParameterTypeExample.EMPTY_ARRAY);
        ParameterExample kwarg1 = new ParameterExample("kwarg1", true, ParameterTypeExample.EMPTY_ARRAY);

        PythonSignature signature = new PythonSignature(new SignatureBase(new ParameterExample[]{arg1}), new ParameterExample[]{kwarg1});

        ParameterExample[] combinedArgs = signature.getCombinedArgs();
        Assert.assertEquals(2, combinedArgs.length);
        Assert.assertEquals(arg1, combinedArgs[0]);
    }
}
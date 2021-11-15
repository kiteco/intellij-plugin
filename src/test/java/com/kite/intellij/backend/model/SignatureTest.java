package com.kite.intellij.backend.model;

import org.junit.Assert;
import org.junit.Test;

public class SignatureTest {
    @Test
    public void testEmptyCombined() throws Exception {
        Signature s1 = new SignatureBase(null);
        Assert.assertEquals(0, s1.getCombinedArgs().length);
    }

    @Test
    public void testSimpleCombined() throws Exception {
        ParameterExample arg1 = new ParameterExample("arg1", false, ParameterTypeExample.EMPTY_ARRAY);
        Signature signature = new SignatureBase(new ParameterExample[]{arg1});

        ParameterExample[] combinedArgs = signature.getCombinedArgs();
        Assert.assertEquals(1, combinedArgs.length);
        Assert.assertEquals(arg1, combinedArgs[0]);
    }
}
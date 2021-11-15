package com.kite.intellij.backend.model;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class PythonFunctionDetailsTest {
    @Test
    public void testBasic() throws Exception {
        PythonFunctionDetails d = new PythonFunctionDetails(new FunctionDetailsBase(Signature.EMPTY_ARRAY, new Parameter[]{param("p1")}, null), param("receiver"), null, null, null, null);

        Assert.assertNotNull("receiver", d.getReceiver());
        Assert.assertEquals("receiver", d.getReceiver().getName());
        Assert.assertTrue(d.isParametersAvailable());
        Assert.assertEquals("p1", d.getParameters()[0].getName());
        Assert.assertFalse(d.hasVararg());
        Assert.assertFalse(d.hasKwarg());
        Assert.assertFalse(d.isReturnAnnotationAvailable());
        Assert.assertFalse(d.isReturnValueAvailable());
    }

    @Test
    public void testEquals() throws Exception {
        FunctionDetails a = new PythonFunctionDetails(new FunctionDetailsBase(Signature.EMPTY_ARRAY, new Parameter[]{param("p1")}, null), param("receiver"), null, null, null, null);
        FunctionDetails b = new PythonFunctionDetails(new FunctionDetailsBase(Signature.EMPTY_ARRAY, new Parameter[]{param("p1")}, null), param("receiver"), null, null, null, null);
        FunctionDetails c = new PythonFunctionDetails(new FunctionDetailsBase(Signature.EMPTY_ARRAY, new Parameter[]{param("p1")}, null), param("receiverOther"), null, null, null, null);

        Assert.assertEquals(a, a);
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);
        Assert.assertEquals(c, c);

        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(c, a);
    }

    private ParameterBase param(String name) {
        return new ParameterBase(name, null, "synopsis");
    }
}

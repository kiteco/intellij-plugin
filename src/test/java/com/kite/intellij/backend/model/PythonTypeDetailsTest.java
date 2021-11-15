package com.kite.intellij.backend.model;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class PythonTypeDetailsTest {
    @Test
    public void testBasic() throws Exception {
        FunctionDetailsBase base = new FunctionDetailsBase(Signature.EMPTY_ARRAY, Parameter.EMPTY_ARRAY, null);
        PythonFunctionDetails constructor = new PythonFunctionDetails(base, null, null, null, null, Parameter.EMPTY_ARRAY);

        PythonTypeDetails t = new PythonTypeDetails(new TypeDetailsBase(0, null), new Base[]{new Base(Id.of("base1"), "base1", "baseModule", Id.of("baseModule"))}, constructor);

        Assert.assertEquals(DetailType.Type, t.getType());
        Assert.assertEquals("base1", t.getBases()[0].getId().getValue());
        Assert.assertEquals(0, t.getTotalMembers());
        Assert.assertFalse(t.hasMembers());
    }

    @Test
    public void testEquals() throws Exception {
        TypeDetails a = new PythonTypeDetails(new TypeDetailsBase(0, null), new Base[]{new Base(Id.of("base1"), "base1", "baseModule", Id.of("baseModule"))}, null);
        TypeDetails b = new PythonTypeDetails(new TypeDetailsBase(0, null), new Base[]{new Base(Id.of("base1"), "base1", "baseModule", Id.of("baseModule"))}, null);

        TypeDetails c = new PythonTypeDetails(new TypeDetailsBase(0, null), new Base[]{new Base(Id.of("base1"), "baseOther", "baseModuleOther", Id.of("baseModuleOther"))}, null);

        Assert.assertEquals(a, a);
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);
        Assert.assertEquals(c, c);

        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(c, a);
    }
}

package com.kite.intellij.backend.json;

import com.kite.intellij.backend.model.PythonTypeDetails;
import com.kite.intellij.backend.model.TypeDetails;
import com.kite.intellij.backend.model.TypeDetailsBase;
import com.kite.intellij.test.KiteTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class TypeJsonParsingTest {
    @Test
    public void testPythonTypeDetails() throws Exception {
        PythonTypeDetails response = new KiteJsonParsing().fromJson(KiteTestUtils.loadTestDataFile("model/json/typeDetails/type_python.json"), TypeDetails.class);
        Assert.assertNotNull(response);

        Assert.assertEquals(53, response.getTotalMembers());

        Assert.assertEquals(5, response.getMembers().length);
        Assert.assertEquals("__builtin__.int.bit_length", response.getMembers()[0].getId().getValue());

        Assert.assertEquals(1, response.getBases().length);
        Assert.assertEquals("__builtin__.object", response.getBases()[0].getId().getValue());
        Assert.assertEquals("object", response.getBases()[0].getName());
    }

    @Test
    public void testPythonNullDetails() throws Exception {
        //this threw an exception
        PythonTypeDetails response = new KiteJsonParsing().fromJson(KiteTestUtils.loadTestDataFile("model/json/typeDetails/python/type_nullDetails.json"), TypeDetails.class);
        Assert.assertNotNull(response);
    }

    @Test
    public void testPythonConstructor() throws Exception {
        //this threw an exception
        PythonTypeDetails response = new KiteJsonParsing().fromJson(KiteTestUtils.loadTestDataFile("model/json/typeDetails/python/type_constructor.json"), TypeDetails.class);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getConstructor());
        Assert.assertTrue(response.hasConstructor());
        Assert.assertTrue(response.getConstructor().hasKwarg());
        Assert.assertEquals(4, response.getConstructor().getKwargParameters().length);
    }

    @Test
    public void testFallbackParsing() throws Exception {
        TypeDetails response = new KiteJsonParsing().fromJson(KiteTestUtils.loadTestDataFile("model/json/typeDetails/type_undefined.json"), TypeDetails.class);
        Assert.assertTrue(response instanceof PythonTypeDetails);
    }
}

package com.kite.intellij.backend.json.deserializer.python;

import com.kite.intellij.backend.json.KiteJsonParsing;
import com.kite.intellij.backend.model.Calls;
import com.kite.intellij.test.KiteTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class PythonParameterDeserializerTest {
    @Test
    public void testNullLangDetails() throws Exception {
        //parsing threw an exception
        Calls calls = new KiteJsonParsing().parseCalls(KiteTestUtils.loadTestDataFile("model/json/signature/python/json.dumps.json"));
        Assert.assertNotNull(calls);

    }
}
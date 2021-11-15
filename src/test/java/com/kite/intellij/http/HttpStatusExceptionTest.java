package com.kite.intellij.http;

import com.kite.intellij.backend.http.HttpStatusException;
import org.junit.Assert;
import org.junit.Test;

/**
 */
public class HttpStatusExceptionTest {
    @Test
    public void testBasic() throws Exception {
        try {
            throw new HttpStatusException("invalid status", 404, "");
        } catch (HttpStatusException e) {
            Assert.assertEquals(404, e.getStatusCode());
        }
    }
}
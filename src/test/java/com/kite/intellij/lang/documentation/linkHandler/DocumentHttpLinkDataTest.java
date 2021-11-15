package com.kite.intellij.lang.documentation.linkHandler;

import org.junit.Assert;
import org.junit.Test;

public class DocumentHttpLinkDataTest {
    @Test
    public void testBasics() throws Exception {
        Assert.assertEquals("http://www.kite.com/", new DocumentHttpLinkData("http://www.kite.com/").getHttpLink());

        try {
            new DocumentHttpLinkData("www.invalid-link.com");
            Assert.fail("Expected a validation error");
        } catch (IllegalArgumentException e) {
            //ignore
        }
    }
}
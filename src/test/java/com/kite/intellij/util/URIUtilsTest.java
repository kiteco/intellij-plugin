package com.kite.intellij.util;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class URIUtilsTest {
    @Test
    public void testQueryParam() throws Exception {
        Assert.assertTrue(URIUtils.getQueryParamValue("http://www.kite.example?foo=bar&foo2=bar2", "foo2").isPresent());

        Assert.assertFalse(URIUtils.getQueryParamValue("http://www.kite.example?foo=bar&foo2=bar2", "notAvailable").isPresent());
    }

    @Test
    public void testInvalidUrl() throws Exception {
        Assert.assertFalse(URIUtils.getQueryParamValue("no-uri", "not-here").isPresent());
    }
}
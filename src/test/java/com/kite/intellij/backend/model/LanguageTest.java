package com.kite.intellij.backend.model;

import com.kite.intellij.lang.KiteLanguage;
import org.junit.Assert;
import org.junit.Test;

public class LanguageTest {
    @Test
    public void testBasics() throws Exception {
        Assert.assertEquals(KiteLanguage.Python, KiteLanguage.fromJson("python"));
        Assert.assertEquals("python", KiteLanguage.Python.jsonName());
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalid() throws Exception {
        KiteLanguage.fromJson("unknown");
    }
}
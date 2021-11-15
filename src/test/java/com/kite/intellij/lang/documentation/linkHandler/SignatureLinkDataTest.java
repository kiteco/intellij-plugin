package com.kite.intellij.lang.documentation.linkHandler;

import org.junit.Assert;
import org.junit.Test;

import java.util.OptionalInt;

public class SignatureLinkDataTest {
    @Test
    public void testExpandKwargs() {
        SignatureLinkData d = new SignatureLinkData(OptionalInt.of(1), false, false, false, false);
        Assert.assertEquals(d.withExpandKwargs(true), d.with("expandKwargs", true));
    }
}
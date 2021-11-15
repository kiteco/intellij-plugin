package com.kite.intellij.backend.model;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class KindTest {
    @Test
    public void testJsonValue() throws Exception {
        Assert.assertEquals(Kind.Descriptor, Kind.fromJsonString("descriptor"));
        Assert.assertEquals(Kind.Function, Kind.fromJsonString("function"));
        Assert.assertEquals(Kind.Module, Kind.fromJsonString("module"));
        Assert.assertEquals(Kind.Instance, Kind.fromJsonString("instance"));
        Assert.assertEquals(Kind.Type, Kind.fromJsonString("type"));
        Assert.assertEquals(Kind.Object, Kind.fromJsonString("object"));
        Assert.assertEquals(Kind.Symbol, Kind.fromJsonString("symbol"));
        Assert.assertEquals(Kind.Union, Kind.fromJsonString("union"));
    }

    @Test
    public void testInvalidValue() throws Exception {
        Assert.assertEquals(Kind.Unknown, Kind.fromJsonString("unsupported value"));
        Assert.assertEquals(Kind.Unknown, Kind.fromJsonString("invalid(5)"));
    }
}
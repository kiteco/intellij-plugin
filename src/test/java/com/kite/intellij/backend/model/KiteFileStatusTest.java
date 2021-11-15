package com.kite.intellij.backend.model;

import org.junit.Assert;
import org.junit.Test;

public class KiteFileStatusTest {
    @Test
    public void testEquals() throws Exception {
        Assert.assertEquals(KiteFileStatus.Indexing, KiteFileStatus.Indexing);

        Assert.assertNotEquals(KiteFileStatus.Indexing, KiteFileStatus.Initializing);
        Assert.assertNotEquals(KiteFileStatus.Indexing, KiteFileStatus.Ready);
        Assert.assertNotEquals(KiteFileStatus.Indexing, KiteFileStatus.NoIndex);
    }

    @Test
    public void testFromJson() {
        Assert.assertEquals(KiteFileStatus.Ready, KiteFileStatus.fromJsonString("ready"));
        Assert.assertEquals(KiteFileStatus.Initializing, KiteFileStatus.fromJsonString("initializing"));
        Assert.assertEquals(KiteFileStatus.Indexing, KiteFileStatus.fromJsonString("indexing"));
        Assert.assertEquals(KiteFileStatus.NoIndex, KiteFileStatus.fromJsonString("noIndex"));
    }

    @Test
    public void testFromJsonInvalid() {
        Assert.assertEquals(KiteFileStatus.Unknown, KiteFileStatus.fromJsonString("unknown value"));
    }
}
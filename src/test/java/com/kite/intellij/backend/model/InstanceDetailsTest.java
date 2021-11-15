package com.kite.intellij.backend.model;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class InstanceDetailsTest {
    @Test
    public void testBasic() throws Exception {
        InstanceDetails o = new InstanceDetails(null);
        Assert.assertEquals(DetailType.Object, o.getType());
        Assert.assertEquals(null, o.getInstanceType());
    }

    @Test
    public void testEquals() throws Exception {
        InstanceDetails a = new InstanceDetails(null);
        InstanceDetails b = new InstanceDetails(null);

        Assert.assertEquals(a, a);
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);
    }
}
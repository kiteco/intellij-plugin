package com.kite.intellij.backend.model;

import com.kite.intellij.backend.response.ValueReportResponse;
import org.junit.Assert;
import org.junit.Test;

/**
 */
public class ValueReportResponseTest {
    @Test
    public void testBasic() throws Exception {
        ValueReportResponse r = new ValueReportResponse(new ValueExt(Id.of("id"), Kind.Type, "", "", "", null, null, null, null), new Report(null, "", "", null, (Usage[]) null, 0));
        Assert.assertEquals("id", r.getValue().getId().getValue());
    }

    @Test
    public void testEquals() throws Exception {
        ValueReportResponse a = new ValueReportResponse(new ValueExt(Id.of("id"), Kind.Type, "", "", "", null, null, null, null), new Report(null, "", "", null, (Usage[]) null, 0));
        ValueReportResponse b = new ValueReportResponse(new ValueExt(Id.of("id"), Kind.Type, "", "", "", null, null, null, null), new Report(null, "", "", null, (Usage[]) null, 0));
        ValueReportResponse c = new ValueReportResponse(new ValueExt(Id.of("id other"), Kind.Type, "", "", "", null, null, null, null), new Report(null, "", "", null, (Usage[]) null, 0));

        Assert.assertEquals(a, a);
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);

        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(c, a);
    }
}
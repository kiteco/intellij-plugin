package com.kite.intellij.backend.response;

import com.kite.intellij.backend.model.Example;
import com.kite.intellij.backend.model.Id;
import com.kite.intellij.backend.model.Report;
import com.kite.intellij.backend.model.SymbolExt;
import org.junit.Assert;
import org.junit.Test;

/**
 */
public class SymbolReportResponseTest {
    @Test
    public void testBasic() throws Exception {
        SymbolReportResponse a = new SymbolReportResponse(new SymbolExt(Id.of("id"), "name", "qname", null, null, ""), new Report(null, "description", "html", (Example[]) null, null, 0));

        Assert.assertNotNull(a.getSymbol());
        Assert.assertNotNull(a.getReport());

        SymbolReportResponse b = new SymbolReportResponse(new SymbolExt(Id.of("id"), "name", "qname", null, null, ""), new Report(null, "description", "html", (Example[]) null, null, 0));
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);

        SymbolReportResponse c = new SymbolReportResponse(new SymbolExt(Id.of("different id"), "name", "qname", null, null, ""), new Report(null, "description", "html", (Example[]) null, null, 0));
        Assert.assertNotEquals(a, c);
    }
}
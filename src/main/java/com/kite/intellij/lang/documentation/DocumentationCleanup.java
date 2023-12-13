package com.kite.intellij.lang.documentation;

import com.kite.intellij.backend.model.Report;
import com.kite.intellij.backend.response.HoverResponse;
import com.kite.intellij.backend.response.SymbolReportResponse;
import com.kite.intellij.backend.response.ValueReportResponse;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.jsoup.safety.Whitelist;

/**
 * Cleans the documentation retrieved from Kite.
 *
  */
class DocumentationCleanup {
    private static final Safelist SAFELIST = Safelist.relaxed()
            .addAttributes(":all", "class", "style", "title")
            .addProtocols("a", "href", "kite", "#");

    private static String cleanup(String html) {
        return Jsoup.clean(html, SAFELIST);
    }

    static HoverResponse cleanup(HoverResponse hover) {
        Report report = hover.getReport();
        if (report != null) {
            String html = report.getDescriptionHtml();

            if (html.startsWith("<body>") && html.endsWith("</body>")) {
                html = html.substring("<body>".length(), html.length() - "</body".length() - 1);
            }

            html = cleanup(html);

            hover = hover.withReport(report.withDescriptionHtml(html));
        }

        return hover;
    }

    static ValueReportResponse cleanup(ValueReportResponse report) {
        Report htmlReport = report.getReport();
        if (htmlReport == null) {
            //mostly for test-cases
            return report;
        }

        return report.withReport(htmlReport.withDescriptionHtml(cleanup(htmlReport.getDescriptionHtml())));
    }

    static SymbolReportResponse cleanup(SymbolReportResponse report) {
        Report htmlReport = report.getReport();
        if (htmlReport == null) {
            //mostly for test-cases
            return report;
        }

        return report.withReport(htmlReport.withDescriptionHtml(cleanup(htmlReport.getDescriptionHtml())));
    }
}

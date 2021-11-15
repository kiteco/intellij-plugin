package com.kite.intellij.action.signatureInfo;

import com.intellij.openapi.diagnostic.Logger;
import org.junit.Test;
import org.xhtmlrenderer.resource.XMLResource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

public class XMLParsingTest {
    private static final Logger LOG = Logger.getInstance(XMLParsingTest.class);

    @Test
    public void testXMLParsing() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = KiteSignaturePopupManager.createDocumentBuilderFactory();
        String xml = "<html>\n" +
                "<head>\n" +
                "    <style type=\"text/css\">\n" +
                "        html, body, div, p, pre, code, h1, h2, h3, h4, h5, table, td, tr, th {\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "            border: none;\n" +
                "            outline: none;\n" +
                "        }\n" +
                "\n" +
                "        html {\n" +
                "            color: #a9b7c6;\n" +
                "                        font-family: \"Lato\",\"sans-serif\";\n" +
                "            font-size: 35px;\n" +
                "        }\n" +
                "\n" +
                "        /* don't render the text style's background as this style is special */\n" +
                "              .style-text.op-33, .op-33 .style-text{color: #54595e;}\n" +
                "        .style-text.op-40, .op-40 .style-text{color: #5d6369;}\n" +
                "        .style-text.op-50, .op-50 .style-text{color: #6a7178;}\n" +
                "        .style-text.op-67, .op-67 .style-text{color: #7f8892;}\n" +
                "        .style-text.op-75, .op-75 .style-text{color: #89949f;}\n" +
                "        .style-text.op-80, .op-80 .style-text{color: #8f9ba7;}\n" +
                "        .style-text, .style-text.op-100, .op-100 .style-text{color: #a9b7c6;}\n" +
                "                .style-call.op-33, .op-33 .style-call{color: #5f666c;}\n" +
                "        .style-call.op-40, .op-40 .style-call{color: #676f76;}\n" +
                "        .style-call.op-50, .op-50 .style-call{color: #727b83;}\n" +
                "        .style-call.op-67, .op-67 .style-call{color: #858f9a;}\n" +
                "        .style-call.op-75, .op-75 .style-call{color: #8d99a4;}\n" +
                "        .style-call.op-80, .op-80 .style-call{color: #939fab;}\n" +
                "        .style-call, .style-call.op-100, .op-100 .style-call{color: #a9b7c6;}\n" +
                "                .style-paren.op-33, .op-33 .style-paren{color: #5f666c;}\n" +
                "        .style-paren.op-40, .op-40 .style-paren{color: #676f76;}\n" +
                "        .style-paren.op-50, .op-50 .style-paren{color: #727b83;}\n" +
                "        .style-paren.op-67, .op-67 .style-paren{color: #858f9a;}\n" +
                "        .style-paren.op-75, .op-75 .style-paren{color: #8d99a4;}\n" +
                "        .style-paren.op-80, .op-80 .style-paren{color: #939fab;}\n" +
                "        .style-paren, .style-paren.op-100, .op-100 .style-paren{color: #a9b7c6;}\n" +
                "                .style-param.op-33, .op-33 .style-param{color: #5f666c;}\n" +
                "        .style-param.op-40, .op-40 .style-param{color: #676f76;}\n" +
                "        .style-param.op-50, .op-50 .style-param{color: #727b83;}\n" +
                "        .style-param.op-67, .op-67 .style-param{color: #858f9a;}\n" +
                "        .style-param.op-75, .op-75 .style-param{color: #8d99a4;}\n" +
                "        .style-param.op-80, .op-80 .style-param{color: #939fab;}\n" +
                "        .style-param, .style-param.op-100, .op-100 .style-param{color: #a9b7c6;}\n" +
                "                .style-comma.op-33, .op-33 .style-comma{color: #6b513c;}\n" +
                "        .style-comma.op-40, .op-40 .style-comma{color: #75553b;}\n" +
                "        .style-comma.op-50, .op-50 .style-comma{color: #845b39;}\n" +
                "        .style-comma.op-67, .op-67 .style-comma{color: #9c6536;}\n" +
                "        .style-comma.op-75, .op-75 .style-comma{color: #a86935;}\n" +
                "        .style-comma.op-80, .op-80 .style-comma{color: #af6c35;}\n" +
                "        .style-comma, .style-comma.op-100, .op-100 .style-comma{color: #cc7832;}\n" +
                "                .style-kw-arg.op-33, .op-33 .style-kw-arg{color: #604238;}\n" +
                "        .style-kw-arg.op-40, .op-40 .style-kw-arg{color: #684336;}\n" +
                "        .style-kw-arg.op-50, .op-50 .style-kw-arg{color: #734433;}\n" +
                "        .style-kw-arg.op-67, .op-67 .style-kw-arg{color: #85452e;}\n" +
                "        .style-kw-arg.op-75, .op-75 .style-kw-arg{color: #8e462c;}\n" +
                "        .style-kw-arg.op-80, .op-80 .style-kw-arg{color: #94472b;}\n" +
                "        .style-kw-arg, .style-kw-arg.op-100, .op-100 .style-kw-arg{color: #aa4926;}\n" +
                "                .style-opsign-arg.op-33, .op-33 .style-opsign-arg{color: #5f666c;}\n" +
                "        .style-opsign-arg.op-40, .op-40 .style-opsign-arg{color: #676f76;}\n" +
                "        .style-opsign-arg.op-50, .op-50 .style-opsign-arg{color: #727b83;}\n" +
                "        .style-opsign-arg.op-67, .op-67 .style-opsign-arg{color: #858f9a;}\n" +
                "        .style-opsign-arg.op-75, .op-75 .style-opsign-arg{color: #8d99a4;}\n" +
                "        .style-opsign-arg.op-80, .op-80 .style-opsign-arg{color: #939fab;}\n" +
                "        .style-opsign-arg, .style-opsign-arg.op-100, .op-100 .style-opsign-arg{color: #a9b7c6;}\n" +
                "                .style-builtin-name.op-33, .op-33 .style-builtin-name{color: #55576c;}\n" +
                "        .style-builtin-name.op-40, .op-40 .style-builtin-name{color: #5a5c76;}\n" +
                "        .style-builtin-name.op-50, .op-50 .style-builtin-name{color: #626383;}\n" +
                "        .style-builtin-name.op-67, .op-67 .style-builtin-name{color: #6e6f9a;}\n" +
                "        .style-builtin-name.op-75, .op-75 .style-builtin-name{color: #7575a4;}\n" +
                "        .style-builtin-name.op-80, .op-80 .style-builtin-name{color: #7879ab;}\n" +
                "        .style-builtin-name, .style-builtin-name.op-100, .op-100 .style-builtin-name{color: #8888c6;}\n" +
                "                .style-bracket.op-33, .op-33 .style-bracket{color: #5f666c;}\n" +
                "        .style-bracket.op-40, .op-40 .style-bracket{color: #676f76;}\n" +
                "        .style-bracket.op-50, .op-50 .style-bracket{color: #727b83;}\n" +
                "        .style-bracket.op-67, .op-67 .style-bracket{color: #858f9a;}\n" +
                "        .style-bracket.op-75, .op-75 .style-bracket{color: #8d99a4;}\n" +
                "        .style-bracket.op-80, .op-80 .style-bracket{color: #939fab;}\n" +
                "        .style-bracket, .style-bracket.op-100, .op-100 .style-bracket{color: #a9b7c6;}\n" +
                "                .style-unused.op-33, .op-33 .style-unused{color: #525455;}\n" +
                "        .style-unused.op-40, .op-40 .style-unused{color: #57595a;}\n" +
                "        .style-unused.op-50, .op-50 .style-unused{color: #5e5f60;}\n" +
                "        .style-unused.op-67, .op-67 .style-unused{color: #696a6b;}\n" +
                "        .style-unused.op-75, .op-75 .style-unused{color: #6f6f70;}\n" +
                "        .style-unused.op-80, .op-80 .style-unused{color: #727373;}\n" +
                "        .style-unused, .style-unused.op-100, .op-100 .style-unused{color: #808080;}\n" +
                "                .style-link.op-33, .op-33 .style-link{color: #355274;}\n" +
                "        .style-link.op-40, .op-40 .style-link{color: #34577f;}\n" +
                "        .style-link.op-50, .op-50 .style-link{color: #325d8f;}\n" +
                "        .style-link.op-67, .op-67 .style-link{color: #2e67aa;}\n" +
                "        .style-link.op-75, .op-75 .style-link{color: #2d6cb6;}\n" +
                "        .style-link.op-80, .op-80 .style-link{color: #2c6fbe;}\n" +
                "        .style-link, .style-link.op-100, .op-100 .style-link{color: #287bde;}\n" +
                "  \n" +
                "        .style-text.active, .active .style-text,\n" +
                "        .style-param.active, .active .style-param,\n" +
                "        .style-kw-arg.active, .active .style-kw-arg {\n" +
                "            background-color: #323232 !important;\n" +
                "            font-style: italic;\n" +
                "        }\n" +
                "\n" +
                "        a.active.style-text {\n" +
                "            background-color: #202020 !important;\n" +
                "            text-decoration: underline;\n" +
                "        }\n" +
                "\n" +
                "        .kite-link, .kite-link-flow, .open-docs-link {\n" +
                "            height: 12px;\n" +
                "            line-height: 12px;\n" +
                "            text-align: right;\n" +
                "        }\n" +
                "\n" +
                "        body.hover .kite-link-flow,\n" +
                "        body.signatures .kite-link-flow {\n" +
                "            text-align: left;\n" +
                "        }\n" +
                "\n" +
                "        .report-section {\n" +
                "            margin-top: 0.75em;\n" +
                "        }\n" +
                "\n" +
                "        .report-content {\n" +
                "            margin-left: 0.5em;\n" +
                "        }\n" +
                "\n" +
                "        .align-right {\n" +
                "            text-align: right;\n" +
                "         }\n" +
                "        .hover-hint {\n" +
                "            color: #626383;\n" +
                "        }\n" +
                "\n" +
                "              a.op-33, .op-33 a{color: #355274;}\n" +
                "        a.op-40, .op-40 a{color: #34577f;}\n" +
                "        a.op-50, .op-50 a{color: #325d8f;}\n" +
                "        a.op-67, .op-67 a{color: #2e67aa;}\n" +
                "        a.op-75, .op-75 a{color: #2d6cb6;}\n" +
                "        a.op-80, .op-80 a{color: #2c6fbe;}\n" +
                "        a, a.op-100, .op-100 a{color: #287bde;}\n" +
                "          a {\n" +
                "            text-decoration: none;\n" +
                "        }\n" +
                "        a:hover {\n" +
                "            text-decoration: underline;\n" +
                "        }\n" +
                "\n" +
                "        .code, code, pre {\n" +
                "            font-family: \"Consolas\", monospace;\n" +
                "        }\n" +
                "\n" +
                "        pre {\n" +
                "            margin: 0.5em 0.75em;\n" +
                "            white-space: pre-wrap;\n" +
                "        }\n" +
                "\n" +
                "        table {\n" +
                "            border-collapse: collapse;\n" +
                "            border-spacing: 0;\n" +
                "            width:100%;\n" +
                "        }\n" +
                "\n" +
                "        .heading table {\n" +
                "            width: auto;\n" +
                "        }\n" +
                "\n" +
                "        .heading td {\n" +
                "            vertical-align: middle;\n" +
                "        }\n" +
                "\n" +
                "        .heading .heading-text {\n" +
                "            color: #7f8892;\n" +
                "        }\n" +
                "\n" +
                "        .heading .heading-text.active {\n" +
                "            color: #a9b7c6;\n" +
                "            font-style: normal;\n" +
                "        }\n" +
                "\n" +
                "        .heading a.toggle-link {\n" +
                "            color: #325d8f;\n" +
                "            margin-left: 1.25em;\n" +
                "        }\n" +
                "\n" +
                "        .content-container {\n" +
                "            background-color: #202020;\n" +
                "            font-size: 0.9em;\n" +
                "            padding: 0.4em 0.6em;\n" +
                "        }\n" +
                "\n" +
                "        body.signatures a.open-docs-link,\n" +
                "        body.signatures a.show-popular-patterns-link,\n" +
                "        body.signatures a.show-kwargs-link {\n" +
                "            color: #325d8f;\n" +
                "            margin-right: 0.75em;\n" +
                "        }\n" +
                "\n" +
                "        a.link {\n" +
                "            font-size: 10px;\n" +
                "        }\n" +
                "\n" +
                "        .heading a.toggle-link:hover,\n" +
                "        body.signatures a.open-docs-link:hover,\n" +
                "        body.signatures a.show-popular-patterns-link:hover,\n" +
                "        body.signatures a.show-kwargs-link:hover {\n" +
                "            color: #287bde;\n" +
                "        }\n" +
                "\n" +
                "        body.hover a.footer-link {\n" +
                "            color: #325d8f;\n" +
                "            margin-right: 0.75em;\n" +
                "        }\n" +
                "\n" +
                "        body.hover a.footer-link:hover {\n" +
                "            color: #287bde;\n" +
                "        }\n" +
                "\n" +
                "        .signature-info, .signature-pattern {\n" +
                "            padding-left: 1em;\n" +
                "            text-indent: -1em;\n" +
                "        }\n" +
                "\n" +
                "        .inferred-kwarg, .signature-pattern {\n" +
                "            margin-top: 0.25em;\n" +
                "        }\n" +
                "\n" +
                "        .signature-pattern .style-param {\n" +
                "            color: #727b83;\n" +
                "        }\n" +
                "\n" +
                "        .signature-pattern .style-kw-arg {\n" +
                "            color: #734433;\n" +
                "        }\n" +
                "\n" +
                "        .signature-pattern .style-opsign-arg {\n" +
                "            color: #727b83;\n" +
                "        }\n" +
                "\n" +
                "        .footer {\n" +
                "            margin-top: 0.75em;\n" +
                "        }\n" +
                "\n" +
                "        .footer td {\n" +
                "            height: 100%;\n" +
                "            vertical-align: middle;\n" +
                "        }\n" +
                "\n" +
                "        .footer td.icon-container {\n" +
                "            width: 12px;\n" +
                "        }\n" +
                "\n" +
                "        .footer td.icon-container img {\n" +
                "            height: 12px;\n" +
                "            vertical-align: middle;\n" +
                "            width: 12px;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body id=\"short\" class=\"signatures\">\n" +
                "    <div class=\"content-container\">\n" +
                "                    \n" +
                "\n" +
                "\n" +
                "<div class=\"signature-info code\">\n" +
                "    <span class=\"function-name style-call\">json.loads</span><span class=\"open-parens style-paren\">(</span><span class=\"op-40\"><span class=\" style-param\">s</span></span><span class=\"style-comma\">,</span><span> </span><span class=\"op-40\"><span class=\" style-kw-arg\">encoding</span><span class=\"style-opsign-arg\">=</span><span class=\"style-param\">None</span></span><span class=\"style-comma\">,</span><span> </span><span class=\"op-40\"><span class=\" style-kw-arg\">cls</span><span class=\"style-opsign-arg\">=</span><span class=\"style-param\">None</span></span><span class=\"style-comma\">,</span><span> </span><span class=\"op-40\"><span class=\" style-kw-arg\">object_hook</span><span class=\"style-opsign-arg\">=</span><span class=\"style-param\">None</span></span><span class=\"style-comma\">,</span><span> </span><span class=\"op-40\"><span class=\" style-kw-arg\">parse_float</span><span class=\"style-opsign-arg\">=</span><span class=\"style-param\">None</span></span><span class=\"style-comma\">,</span><span> </span><span class=\"op-40\"><span class=\" style-kw-arg\">parse_int</span><span class=\"style-opsign-arg\">=</span><span class=\"style-param\">None</span></span><span class=\"style-comma\">,</span><span> </span><span class=\"op-40\"><span class=\" style-kw-arg\">parse_constant</span><span class=\"style-opsign-arg\">=</span><span class=\"style-param\">None</span></span><span class=\"style-comma\">,</span><span> </span><span class=\"op-40\"><span class=\" style-kw-arg\">object_pairs_hook</span><span class=\"style-opsign-arg\">=</span><span class=\"style-param\">None</span></span><span class=\"style-comma\">,</span><span> </span><span class=\"op-100\"><span class=\"active style-kw-arg\">**kwds</span></span><span class=\"style-paren\">)</span>&#160;→&#160;<span class=\"style-builtin-name op-67\"><span >dict</span> | <span >list</span> | <span >float</span> | <span >int</span> | <span >unicode</span></span></div>\n" +
                "\n" +
                "        \n" +
                "    \n" +
                "\n" +
                "    \n" +
                "    \n" +
                "    \n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "                <div class=\"footer\">\n" +
                "            <div class=\"kite-link-flow\">\n" +
                "                <table>\n" +
                "                    <tr><td class=\"text\">\n" +
                "                                <a class=\"link open-docs-link\" href=\"kite://docs/python;;;;json.loads\">Docs</a>\n" +
                "\n" +
                "        \n" +
                "                        <a class=\"link toggle-link show-kwargs-link style-text active\" href=\"kite-internal:/signature?expandPopularPatterns=false&amp;expandKwarg=true&amp;inKwargs=true\">**kwds</a>\n" +
                "            \n" +
                "\n" +
                "    \n" +
                "    \n" +
                "    \n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "            <a class=\"link toggle-link show-popular-patterns-link\" href=\"kite-internal:/signature?expandPopularPatterns=true&amp;expandKwarg=false&amp;inKwargs=true\">Examples</a>\n" +
                "    </td><td class=\"icon-container\"><img class=\"kite-logo\" src=\"file:/home/user/.IntelliJIdea2019.1/system/plugins-sandbox-181.1/plugins/kite-plugin/classes/icons/kite_mini_disabled.png\" /></td>\n" +
                "                    </tr>\n" +
                "                </table>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        </div>\n" +
                "</body>\n" +
                "</html>\n";

        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        for (int i = 0; i < 10; i++) {
            long start = System.currentTimeMillis();
            for (int x = 0; x < 100; x++) {
                documentBuilder.parse(new InputSource(new StringReader(xml)));
            }
            long builderTime = System.currentTimeMillis() - start;

            start = System.currentTimeMillis();
            for (int x = 0; x < 100; x++) {
                XMLResource.load(new InputSource(new StringReader(xml)));
            }
            long saucerTime = System.currentTimeMillis() - start;

            LOG.warn(String.format("Factory %s <-> %d Flyingsaucer", builderTime, saucerTime));
        }
    }
}

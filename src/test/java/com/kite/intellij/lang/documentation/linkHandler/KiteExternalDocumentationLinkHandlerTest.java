package com.kite.intellij.lang.documentation.linkHandler;

import com.jetbrains.python.PythonFileType;
import com.kite.intellij.backend.MockKiteApiService;
import com.kite.intellij.lang.documentation.LinksHandlers;
import com.kite.intellij.lang.documentation.PebbleDocumentationRenderer;
import com.kite.intellij.platform.fs.UnixCanonicalPath;
import com.kite.intellij.test.KiteLightFixtureTest;
import com.kite.intellij.util.KiteBrowserUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

/**
 */
@SuppressWarnings({"unchecked", "OptionalGetWithoutIsPresent"})
public class KiteExternalDocumentationLinkHandlerTest extends KiteLightFixtureTest {
    @Test
    public void testBasics() {
        ExternalDocumentationLinkData data = new ExternalDocumentationLinkData("myId");
        LinkHandler<ExternalDocumentationLinkData, ?> handler = LinksHandlers.findMachingLinkHandler(data).get();

        Assert.assertTrue(handler.supportsLink(handler.asLink(data)));
    }

    @Test
    public void testLink() {
        myFixture.configureByText(PythonFileType.INSTANCE, "");

        ExternalDocumentationLinkData linkData = new ExternalDocumentationLinkData("json.dumps");


        LinkHandler<ExternalDocumentationLinkData, ?> handler = LinksHandlers.findMachingLinkHandler(linkData).get();
        Assert.assertEquals("kite-internal:/externalDocs?id=json.dumps", handler.asLink(linkData));

        Assert.assertTrue(handler.supportsLink(handler.asLink(linkData)));
        Assert.assertTrue(handler.supports(linkData));
        handler.render(Optional.empty(), linkData, new LinkRenderContext(myFixture.getEditor(), new UnixCanonicalPath("/home/user/test.py")), new PebbleDocumentationRenderer());

        Assert.assertEquals(1, KiteBrowserUtil.openedUrls.size());
        Assert.assertEquals("http://localhost:46624/clientapi/desktoplogin?d=%2Fpython%2Fdocs%2Fjson.dumps", KiteBrowserUtil.openedUrls.get(0));
    }
}
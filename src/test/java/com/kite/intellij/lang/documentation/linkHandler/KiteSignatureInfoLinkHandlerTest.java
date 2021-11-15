package com.kite.intellij.lang.documentation.linkHandler;

import com.jetbrains.python.PythonFileType;
import com.kite.intellij.backend.model.Call;
import com.kite.intellij.backend.model.Calls;
import com.kite.intellij.lang.documentation.LinksHandlers;
import com.kite.intellij.lang.documentation.PebbleDocumentationRenderer;
import com.kite.intellij.settings.KiteSettingsService;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;
import java.util.OptionalInt;

@SuppressWarnings("unchecked")
public class KiteSignatureInfoLinkHandlerTest extends KiteLightFixtureTest {
    @Test
    public void testBasics() {
        SignatureLinkData data = new SignatureLinkData(OptionalInt.empty(), true, true, true, false);
        LinkHandler<SignatureLinkData, ?> handler = LinksHandlers.findMachingLinkHandler(data).get();

        Assert.assertTrue(handler.supportsLink(handler.asLink(data)));

        Assert.assertEquals("kite-internal:/signature?expandPopularPatterns=true&expandKwarg=true&inKwargs=true", handler.asLink(new SignatureLinkData(OptionalInt.empty(), true, true, true, false)));
        Assert.assertEquals("kite-internal:/signature?expandPopularPatterns=false&expandKwarg=true", handler.asLink(new SignatureLinkData(OptionalInt.empty(), false, true, false, false)));
        Assert.assertEquals("kite-internal:/signature?expandPopularPatterns=true&expandKwarg=false&argIndex=10", handler.asLink(new SignatureLinkData(OptionalInt.of(10), false, false, true, false)));

        Assert.assertEquals(new SignatureLinkData(OptionalInt.of(10), false, true, true, false), handler.createLinkData(handler.asLink(new SignatureLinkData(OptionalInt.of(10), false, true, true, false))));
    }

    @Test
    public void testFontSizeOverride() {
        myFixture.configureByText(PythonFileType.INSTANCE, "print()");

        KiteSignatureInfoLinkHandler handler = new KiteSignatureInfoLinkHandler();

        PebbleDocumentationRenderer renderer = new PebbleDocumentationRenderer();
        LinkRenderContext linkRenderContext = LinkRenderContext.create(myFixture.getEditor());

        SignatureLinkData linkData = new SignatureLinkData(OptionalInt.of(0), false, false, true, false);
        Optional<String> noOverrideHtml = handler.render(new Calls(Call.EMPTY_ARRAY), linkData, linkRenderContext, renderer);

        KiteSettingsService.getInstance().getState().paramInfoFontSizeEnabled = true;
        KiteSettingsService.getInstance().getState().paramInfoFontSize = 42; //value must not be default value on any OS
        Optional<String> overrideHtml = handler.render(new Calls(Call.EMPTY_ARRAY), linkData, linkRenderContext, renderer);

        Assert.assertNotEquals("The font settings must affect the rendering", noOverrideHtml.orElse(""), overrideHtml.orElse(""));
    }

    @Test
    public void testNullFilePath() throws Exception {
        KiteSignatureInfoLinkHandler handler = new KiteSignatureInfoLinkHandler();

        Optional<?> response = handler.rawResponseData(new SignatureLinkData(OptionalInt.empty(), false, false, false, false), null);
        Assert.assertFalse(response.isPresent());
    }
}
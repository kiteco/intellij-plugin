package com.kite.intellij.lang.documentation.linkHandler;

import com.kite.intellij.backend.KiteApiService;
import com.kite.intellij.backend.http.HttpTimeoutConfig;
import com.kite.intellij.backend.http.KiteHttpException;
import com.kite.intellij.backend.model.Calls;
import com.kite.intellij.lang.documentation.KiteDocumentationRenderer;
import com.kite.intellij.lang.documentation.KiteLinkHandlerUtils;
import com.kite.intellij.settings.KiteSettings;
import com.kite.intellij.settings.KiteSettingsService;
import org.apache.http.client.utils.URIBuilder;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Handles links to function signature information.
 */
public class KiteSignatureInfoLinkHandler implements LinkHandler<SignatureLinkData, Calls> {
    public KiteSignatureInfoLinkHandler() {
    }

    @Override
    public boolean supportsLink(@Nonnull String link) {
        return link.startsWith(LINK_SCHEME + ":/signature");
    }

    @Override
    public boolean supports(KiteLinkData linkData) {
        return linkData instanceof SignatureLinkData;
    }

    @Override
    public String asLink(@Nonnull SignatureLinkData linkData) {
        URIBuilder uri = createBaseURI().setPath("/signature");
        uri.addParameter("expandPopularPatterns", String.valueOf(linkData.isExpandPopularPatterns()));
        uri.addParameter("expandKwarg", String.valueOf(linkData.isExpandKwargs()));

        linkData.getArgIndex().ifPresent(i -> uri.addParameter("argIndex", String.valueOf(i)));

        if (linkData.isInKwargs()) {
            uri.addParameter("inKwargs", String.valueOf(linkData.isInKwargs()));
        }

        return uri.toString();
    }

    @Override
    public SignatureLinkData createLinkData(@Nonnull String link) {
        URI uri = URI.create(link);
        Map<String, String> paramMap = KiteLinkHandlerUtils.computeQueryParams(uri);

        KiteSettingsService settingsService = KiteSettingsService.getInstance();
        KiteSettings settings = settingsService.getState();

        OptionalInt argIndex = paramMap.containsKey("argIndex") ? OptionalInt.of(Integer.valueOf(paramMap.get("argIndex"))) : OptionalInt.empty();
        boolean isInKwargs = Boolean.valueOf(paramMap.getOrDefault("inKwargs", "false"));
        boolean expandKwargs = Boolean.valueOf(paramMap.getOrDefault("expandKwarg", Boolean.toString(settings.showKwargs)));
        boolean expandPopularPatterns = Boolean.valueOf(paramMap.getOrDefault("expandPopularPatterns", Boolean.toString(settings.showPopularPatterns)));

        return new SignatureLinkData(argIndex, isInKwargs, expandKwargs, expandPopularPatterns, false);
    }

    @Override
    public Optional<Calls> rawResponseData(SignatureLinkData linkData, LinkRenderContext renderContext) throws KiteHttpException {
        if (renderContext == null) {
            return Optional.empty();
        }

        KiteApiService api = KiteApiService.getInstance();
        return Optional.ofNullable(api.signatures(renderContext.getFilePath(), renderContext.getEditorContent(), renderContext.getCaretOffset(), HttpTimeoutConfig.DefaultTimeout));
    }

    @Override
    public Optional<String> render(Optional<Calls> calls, @Nonnull SignatureLinkData linkData, LinkRenderContext renderContext, KiteDocumentationRenderer renderer) {
        if (calls.isPresent()) {
            KiteSettings state = KiteSettingsService.getInstance().getState();
            OptionalInt fontSize = state.paramInfoFontSizeEnabled ? OptionalInt.of(state.paramInfoFontSize) : OptionalInt.empty();

            return Optional.of(renderer.render(calls.get(), linkData, fontSize));
        }

        return Optional.empty();
    }

    @Override
    public void postRender(String link, LinkRenderContext renderContext) {
        Map<String, String> paramMap = KiteLinkHandlerUtils.computeQueryParams(link);

        String expandKwarg = paramMap.get("expandKwarg");
        if (expandKwarg != null) {
            KiteSettingsService.getInstance().getState().showKwargs = Boolean.parseBoolean(expandKwarg);
        }

        String expandPopularPatterns = paramMap.get("expandPopularPatterns");
        if (expandPopularPatterns != null) {
            KiteSettingsService.getInstance().getState().showPopularPatterns = Boolean.parseBoolean(expandPopularPatterns);
        }
    }
}

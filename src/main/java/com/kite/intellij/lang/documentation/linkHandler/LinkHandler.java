package com.kite.intellij.lang.documentation.linkHandler;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.kite.intellij.backend.http.KiteHttpException;
import com.kite.intellij.lang.documentation.KiteDocumentationRenderer;
import org.apache.http.client.utils.URIBuilder;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Handles a class of links.
 *
 * @param <T> The supported type of link data
 * @param <D> The type of the rendererd data
  */
public interface LinkHandler<T extends KiteLinkData, D> {
    ExtensionPointName<LinkHandler> EP_NAME = ExtensionPointName.create("com.kite.intellij.kiteLinkHandler");
    String LINK_SCHEME = "kite-internal";

    boolean supportsLink(@Nonnull String link);

    boolean supports(KiteLinkData linkData);

    String asLink(@Nonnull T linkData);

    T createLinkData(@Nonnull String link);

    /**
     * Retrieve JSON data from the Kite backend and parse it into the requested type.
     * This method may be run in a separate thread, i.e. it does not have to be executed on the Swing event dispatch thread.
     *
     * @param linkData
     * @param renderContext
     * @return
     * @throws KiteHttpException
     */
    Optional<D> rawResponseData(T linkData, LinkRenderContext renderContext) throws KiteHttpException;

    Optional<String> render(Optional<D> data, @Nonnull T linkData, LinkRenderContext renderContext, KiteDocumentationRenderer renderer);

    default Optional<String> render(D data, @Nonnull T linkData, LinkRenderContext renderContext, KiteDocumentationRenderer renderer) {
        return render(Optional.of(data), linkData, renderContext, renderer);
    }

    /**
     * Must be invoked in the UI EDT thread after the call to render.
     */
    default void postRender(String link, LinkRenderContext renderContext) {
    }

    default URIBuilder createBaseURI() {
        return new URIBuilder().setScheme(LINK_SCHEME);
    }
}

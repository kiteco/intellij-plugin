package com.kite.intellij.ui.html;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.kite.intellij.action.FallbackToOriginalException;
import com.kite.intellij.backend.http.HttpStatusException;
import com.kite.intellij.backend.http.KiteHttpException;
import com.kite.intellij.editor.events.KiteEventQueue;
import com.kite.intellij.lang.documentation.KiteDocumentationRenderer;
import com.kite.intellij.lang.documentation.linkHandler.KiteLinkData;
import com.kite.intellij.lang.documentation.linkHandler.LinkHandler;
import com.kite.intellij.lang.documentation.linkHandler.LinkRenderContext;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

@SuppressWarnings("unchecked")
public class RenderUtil {
    private static final Logger LOG = Logger.getInstance("#kite.renderUtil");

    /**
     * Renders HTMl for the given link using the defined link handler and renderContext.
     *
     * @param link
     * @param linkHandler
     * @param renderContext
     * @param renderer
     * @param project
     */
    public static Optional<String> renderHtml(String link, @Nonnull Optional<LinkHandler> linkHandler, LinkRenderContext renderContext, KiteDocumentationRenderer renderer, Project project) {
        if (!linkHandler.isPresent()) {
            return Optional.empty();
        }

        Optional<String> htmlOpt;
        LinkHandler handler = linkHandler.get();
        try {
            KiteLinkData linkData = handler.createLinkData(link);

            Optional<?> data = KiteEventQueue.getInstance(project).runWhenEmpty(() -> handler.rawResponseData(linkData, renderContext));

            //there are link handler which can handle an empty data optional
            htmlOpt = handler.render(data, linkData, renderContext, renderer);

            //missing hover
            if (!htmlOpt.isPresent()) {
                return Optional.empty();
            }

            return htmlOpt;
        } catch (KiteHttpException e) {
            LOG.warn("Kite data is unavailable. Throwing FallbackToOriginalException: " + e);

            //404 means "nothing found", don't show a popup or fallback in that case
            if (e instanceof HttpStatusException && ((HttpStatusException) e).isNotFoundError404()) {
                return Optional.empty();
            }

            //fallback to IntellIJ's behaviour in all other cases
            throw new FallbackToOriginalException("Error requesting documentation", e);
        } catch (InterruptedException | TimeoutException e) {
            return Optional.empty();
        }
    }
}

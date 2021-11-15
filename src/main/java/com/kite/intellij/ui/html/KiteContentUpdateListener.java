package com.kite.intellij.ui.html;

import com.kite.intellij.lang.documentation.linkHandler.KiteLinkData;
import com.kite.intellij.lang.documentation.linkHandler.LinkHandler;

/**
 * A listener to enable an owner of a {@link KiteHtmlTextPopup} to react to content updates triggered by links.
 * <p>
 * Listeners must be called in the AWT/Swing dispatch thread.
 *
  */
public interface KiteContentUpdateListener {
    /**
     * This method will be invoked after the content provided by the link has been updated and rendered in the component.
     *  @param linkData         The url which was loaded into the component
     * @param linkHandler The link handler used to process the given url
     */
    void contentUpdated(KiteLinkData linkData, LinkHandler<?, ?> linkHandler);
}

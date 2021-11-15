package com.kite.intellij.status;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.ui.components.labels.LinkListener;
import com.kite.intellij.util.KiteBrowserUtil;

/**
 * Link listener for {@link LinkLabel} which opens the link data in the system's browser.
 *
  */
class BrowseLinkListener implements LinkListener<String> {
    private static final Logger LOG = Logger.getInstance("#kite.link.linkListener");

    @Override
    public void linkSelected(LinkLabel source, String linkData) {
        if (source == null || source.isEnabled()) {
            if (linkData != null && !linkData.isEmpty()) {
                KiteBrowserUtil.browse(linkData);
            } else {
                LOG.warn("Empty link found for LinkLabel " + source);
            }
        }
    }
}

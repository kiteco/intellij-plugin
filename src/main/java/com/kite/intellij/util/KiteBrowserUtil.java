package com.kite.intellij.util;

import com.google.common.collect.Lists;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.TestOnly;

import java.net.URI;
import java.util.List;

/**
 */
public class KiteBrowserUtil {
    @TestOnly
    public static List<String> openedUrls;

    static {
        Application application = ApplicationManager.getApplication();
        if (application == null || application.isUnitTestMode()) {
            openedUrls = Lists.newLinkedList();
        }
    }

    private KiteBrowserUtil() {
    }

    public static void browse(String url) {
        if (openedUrls != null) {
            openedUrls.add(url);
        }

        Application app = ApplicationManager.getApplication();
        if (app == null || app.isUnitTestMode()) {
            return;
        }

        if (url.startsWith("kite://")) {
            DesktopFileUtil.open(url);
        } else {
            BrowserUtil.browse(url);
        }
    }

    public static void reset() {
        if (openedUrls != null) {
            openedUrls.clear();
        }
    }

    public static void browse(URI url) {
        if (openedUrls != null) {
            openedUrls.add(url.toString());
        }

        Application app = ApplicationManager.getApplication();
        if (app == null || app.isUnitTestMode()) {
            return;
        }

        BrowserUtil.browse(url);
    }
}

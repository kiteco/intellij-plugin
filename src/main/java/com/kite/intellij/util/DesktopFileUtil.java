package com.kite.intellij.util;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;

import javax.annotation.Nonnull;
import java.io.IOException;

public final class DesktopFileUtil {
    private static final Logger LOG = Logger.getInstance("#kite.platform");

    private DesktopFileUtil() {
    }

    public static boolean open(@Nonnull String urlOrFile) {
        try {
            openProcess(urlOrFile);
            return true;
        } catch (IOException e) {
            LOG.debug("Failure while opening url or file: " + urlOrFile);
            return false;
        }
    }

    protected static void openProcess(String urlOrFile) throws IOException {
        if (SystemInfo.isWindows) {
            // start "" /B "url" means no title, no new window, open URL
            Runtime.getRuntime().exec(new String[]{"cmd", "/C", "start", "\"\"", "/B", urlOrFile});
            return;
        }

        if (SystemInfo.isMac) {
            Runtime.getRuntime().exec(new String[]{"open", urlOrFile});
            return;
        }

        if (SystemInfo.isLinux) {
            BrowserUtil.open(urlOrFile);
        }
    }
}

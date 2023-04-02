package com.kite.intellij.platform;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;

/**
 * Provides information about the currently supported platforms.
 *
  */
public class KitePlatform {
    private static final Logger LOG = Logger.getInstance("#kite.platform");

    /**
     * Returns whether the current platform is supported.
     * At this time Mac OS X >= Yesomite, Windows >= Win7 64bit and Linux are supported.
     *
     * @return {@code true} if the platform is supported, {@code false} otherwise.
     */
    public static boolean isOsVersionSupported() {
        if (SystemInfo.isMac) {
            LOG.debug("OS: Mac" + SystemInfo.getMacOSVersionCode());
            return SystemInfo.isMacOSYosemite;
        }

        if (SystemInfo.isWindows) {
            return true;
        }

        if (SystemInfo.isLinux) {
            LOG.debug("OS: Linux");
            return true; //fixme check for 32/64-bit support of the kite packages
        }

        LOG.debug("OS: Neither Windows, macOS or Linux: " + System.getProperty("os.name") + ", " + System.getProperty("os.version"));
        return false;
    }

    public static boolean isOsSupported() {
        return SystemInfo.isMac || SystemInfo.isWindows || SystemInfo.isLinux;
    }

    /**
     * @return {@code true} if the current user's platform is not supported, {@code false} otherwise.
     */
    public static boolean isOsVersionNotSupported() {
        return !isOsVersionSupported();
    }
}
                            
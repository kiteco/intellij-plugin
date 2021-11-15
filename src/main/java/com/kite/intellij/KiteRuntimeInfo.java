package com.kite.intellij;

import com.google.common.collect.Maps;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Map;

/**
  * @see PluginInfo
 */
public class KiteRuntimeInfo implements PluginInfo {
    private volatile static Boolean testIsHighDpi;
    private PluginDescriptor pluginDescriptor;

    public KiteRuntimeInfo() {
    }

    public static KiteRuntimeInfo getInstance() {
        return PluginInfo.EP_NAME.findExtensionOrFail(KiteRuntimeInfo.class);
    }

    @Nonnull
    static Map<String, Object> reportPluginProperties(@Nullable String pluginVersion) {
        Map<String, Object> baseProperties = Maps.newLinkedHashMap();
        baseProperties.put("os", osName());
        baseProperties.put("os_version", System.getProperty("os.version"));
        baseProperties.put("os_arch", System.getProperty("os.arch"));
        baseProperties.put("editor_platform", ApplicationInfo.getInstance().getVersionName());
        baseProperties.put("editor_productCode", ApplicationInfo.getInstance().getBuild().getProductCode());
        baseProperties.put("editor_version", ApplicationInfo.getInstance().getBuild().asString());
        baseProperties.put("kite_plugin_version", pluginVersion != null ? pluginVersion : "unknown");
        baseProperties.put("java_version", System.getProperty("java.version"));
        baseProperties.put("java_vendor", System.getProperty("java.vendor"));

        return baseProperties;
    }

    public static String osName() {
        String osName;
        if (SystemInfo.isWindows) {
            osName = "win";
        } else if (SystemInfo.isMac) {
            osName = "darwin";
        } else if (SystemInfo.isLinux) {
            osName = "linux";
        } else {
            osName = System.getProperty("os.name");
        }
        return osName;
    }

    public static boolean isHighDpiScreen() {
        if (testIsHighDpi != null) {
            return testIsHighDpi;
        }

        boolean isJreHiDPI = false;
        // newer IntelliJ builds contain several distict scales in JBUI
        // UIUtil.isJreHiDPIEnabled() is used to find out if the JRE is running in HiDPI mode
        // this is not available in 145.x and possibly some newer builds
        try {
            Method method = UIUtil.class.getMethod("isJreHiDPIEnabled");
            Object boolResult = method.invoke(null);
            if (boolResult instanceof Boolean) {
                isJreHiDPI = Boolean.TRUE.equals(boolResult);
            }
        } catch (Exception e) {
            //ignored
        }

        return isJreHiDPI || JBUIScale.isUsrHiDPI() || com.intellij.util.ui.UIUtil.isRetina();
    }

    @TestOnly
    public static void setHighDpiScreen(boolean isHighDpi) {
        assert ApplicationManager.getApplication() == null || ApplicationManager.getApplication().isUnitTestMode();

        testIsHighDpi = isHighDpi;
    }

    @Override
    public void setPluginDescriptor(@NotNull PluginDescriptor pluginDescriptor) {
        this.pluginDescriptor = pluginDescriptor;
    }

    @Nullable
    public String getVersion() {
        return pluginDescriptor instanceof IdeaPluginDescriptor ? ((IdeaPluginDescriptor) pluginDescriptor).getVersion() : null;
    }
}

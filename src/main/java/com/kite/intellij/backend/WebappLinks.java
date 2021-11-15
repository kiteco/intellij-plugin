package com.kite.intellij.backend;

import com.intellij.openapi.application.ApplicationInfo;
import com.kite.intellij.KiteConstants;
import org.apache.http.client.utils.URIBuilder;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import java.net.URISyntaxException;

/**
 * Provides links to access the local and remote pages of Kite. All URLs are suitable to be opened in a browser.
 * The either directly link to the page or initiate a redirect in the browser to open the final page.
 *
  */
public class WebappLinks {
    private static final WebappLinks instance = new WebappLinks(KiteConstants.DEFAULT_HOST, KiteConstants.DEFAULT_PORT);

    private final String host;
    private final int port;

    private WebappLinks(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Contract(pure = true)
    public static WebappLinks getInstance() {
        return instance;
    }

    public String copilotURL() {
        return "https://www.kite.com/copilot/";
    }

    /**
     * @return Returns the URL to the page which displays Kite's settings.
     */
    public String settingsPage() {
        return kiteLinkBuilder("/settings").toString();
    }

    /**
     * @return Returns the URL to the page which displays Kite's account settings.
     */
    public String accountPage() {
        return remoteLink("/settings/account");
    }

    /**
     * @return Returns an URL to log into Kite.
     */
    public String downloadPage(boolean fromNotification) {
        String productCode = ApplicationInfo.getInstance().getBuild().getProductCode();
        return String.format("https://www.kite.com/install/?utm_medium=editor&utm_source=intellij&utm_content=%s&editor=%s",
                fromNotification ? "notification" : "status",
                productCode);
    }

    /**
     * @return Returns an URL to log into Kite.
     */
    public String loginPage() {
        return settingsPage();
    }

    /**
     * @return Returns an URL which displays Kite's online help.
     */
    public String helpPage() {
        return KiteConstants.KITE_HELP_URL;
    }

    public String redirectUrl(@Nonnull RedirectPath path) {
        return redirect(path);
    }

    public String searchPythonDocs() {
        return remoteLink("/python/docs");
    }

    public String symbolDocs(String id) {
        return remoteLink("/python/docs/" + id);
    }

    public String pythonExample(String id) {
        return remoteLink("/examples/python/" + id);
    }

    public String shareKite() {
        return remoteLink("/invite");
    }

    public String upgradeKite() {
        return "https://www.kite.com/pro/";
    }

    private String remoteLink(@Nonnull String path) {
        return linkBuilder("/clientapi/desktoplogin").addParameter("d", path).toString();
    }

    private String redirect(@Nonnull RedirectPath path) {
        String subPath = path.getUrlPath();
        return linkBuilder("/redirect/" + (subPath.startsWith("/") ? subPath.substring(1) : "")).toString();
    }

    private String link(@Nonnull String path) {
        return linkBuilder(path).toString();
    }

    private URIBuilder linkBuilder(@Nonnull String path) {
        return new URIBuilder().setHost(host).setPort(port).setScheme("http").setPath(path);
    }

    /**
     * Returns a link of the form "kite://path".
     *
     * @param path
     * @return
     */
    private URIBuilder kiteLinkBuilder(@Nonnull String path) {
        try {
            //we need to return "kite://path" and not "kite:/path" as setPath(path) would return.
            return new URIBuilder("kite:/" + path);
        } catch (URISyntaxException e) {
            throw new RuntimeException("kite: URL not parsed");
        }
    }

    /**
     * Kite's redirect request supports the targets defined here.
     *
          */
    public enum RedirectPath {
        /**
         * Opens a page to invite people to Kite.
         */
        Invite("/invite");

        private final String path;

        RedirectPath(String path) {
            this.path = path;
        }

        String getUrlPath() {
            return path;
        }
    }
}

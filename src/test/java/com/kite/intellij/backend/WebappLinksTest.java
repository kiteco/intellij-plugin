package com.kite.intellij.backend;

import org.junit.Assert;
import org.junit.Test;

public class WebappLinksTest {
    @Test
    public void testLinks() {
        Assert.assertEquals("kite://settings", WebappLinks.getInstance().settingsPage());
        Assert.assertEquals("kite://settings", WebappLinks.getInstance().loginPage());
        Assert.assertEquals("http://localhost:46624/clientapi/desktoplogin?d=%2Fsettings%2Faccount", WebappLinks.getInstance().accountPage());
        Assert.assertEquals("http://localhost:46624/redirect/invite", WebappLinks.getInstance().redirectUrl(WebappLinks.RedirectPath.Invite));

        Assert.assertEquals("http://localhost:46624/clientapi/desktoplogin?d=%2Fpython%2Fdocs", WebappLinks.getInstance().searchPythonDocs());

        Assert.assertEquals("http://localhost:46624/clientapi/desktoplogin?d=%2Fpython%2Fdocs%2Fpython%3Bjson.dumps", WebappLinks.getInstance().symbolDocs("python;json.dumps"));

        Assert.assertEquals("http://localhost:46624/clientapi/desktoplogin?d=%2Fexamples%2Fpython%2Fpython%3Bjson.dumps", WebappLinks.getInstance().pythonExample("python;json.dumps"));

        Assert.assertEquals("https://help.kite.com/category/45-intellij-pycharm-integration", WebappLinks.getInstance().helpPage());
    }
}
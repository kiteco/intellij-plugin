package com.kite.testrunner.actions;

import com.intellij.openapi.diagnostic.Logger;
import com.kite.intellij.backend.http.test.MockKiteHttpConnection;
import com.kite.testrunner.TestContext;
import com.kite.testrunner.model.TestRoute;

public class UpdateRouteAction implements TestAction {
    private static final Logger LOG = Logger.getInstance("#kite.testRunner");

    @Override
    public String getId() {
        return "update_route";
    }

    @Override
    public void run(TestContext context) throws Throwable {
        LOG.debug("Applying new route");

        TestRoute route = context.getStep().parseProperties(context, TestRoute.class);
        MockKiteHttpConnection mockHTTP = MockKiteHttpConnection.getInstance();
        route.applyTo(context, mockHTTP);
    }

    @Override
    public boolean runInEventDispatchThread() {
        // we wait after typing, this must not block the EDT
        return false;
    }
}

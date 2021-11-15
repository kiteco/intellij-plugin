package com.kite.testrunner.actions;

import com.kite.testrunner.TestContext;

import java.util.Collections;
import java.util.List;

public class UpdateBlacklistAction implements TestAction {
    @Override
    public String getId() {
        return "update_blacklist";
    }

    @Override
    public void run(TestContext context) throws Throwable {
        List<String> blacklist = context.getStep().getStringListProperty("blacklist", Collections.emptyList());
        context.updateWhitelist(blacklist);
    }
}

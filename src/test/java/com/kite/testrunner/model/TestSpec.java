package com.kite.testrunner.model;

import javax.annotation.Nullable;

public class TestSpec {
    public String description;
    @Nullable
    public Boolean live_environment;
    public TestSetup setup;
    public TestStep[] test;
    public boolean ignore;
}

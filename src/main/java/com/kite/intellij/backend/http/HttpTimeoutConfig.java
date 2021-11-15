package com.kite.intellij.backend.http;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.kite.intellij.KiteConstants;

public enum HttpTimeoutConfig {
    MinimalTimeout, ShortTimeout, DefaultTimeout, LongTimeout;

    public long timeoutMillis() {
        Application application = ApplicationManager.getApplication();
        if (application != null && application.isUnitTestMode()) {
            //the default timeouts are a bit short for heavy load on a test machine
            if (this == MinimalTimeout) {
                return 750;
            }
            if (this == ShortTimeout) {
                return 1000;
            }
        }

        switch (this) {
            case MinimalTimeout:
                return KiteConstants.SO_TIMEOUT_MILLIS_MINIMAL;
            case ShortTimeout:
                return KiteConstants.SO_TIMEOUT_MILLIS_SHORT;
            case DefaultTimeout:
                return KiteConstants.SO_TIMEOUT_MILLIS_DEFAULT;
            case LongTimeout:
                return KiteConstants.SO_TIMEOUT_MILLIS_LONG;
            default:
                throw new IllegalStateException("Unsupported timeout value " + this);
        }
    }
}

package com.kite.intellij.backend.model;

import com.intellij.openapi.diagnostic.Logger;
import com.kite.intellij.platform.fs.CanonicalFilePath;

/**
 * Defines Kite's indexing status. This is usally returned by
 * {@link com.kite.intellij.backend.KiteApiService#fileStatus(CanonicalFilePath)}
 * and is only meaningful and valid for a given file.
 *
  */
public enum KiteFileStatus {
    Ready,
    Initializing,
    NoIndex,
    Indexing,
    Unsupported,
    Error,
    Unauthorized,
    Locked,
    Unknown;

    private static final Logger LOG = Logger.getInstance("#kite.fileStatus");

    public static KiteFileStatus fromJsonString(String status) {
        switch (status) {
            case "ready":
                return Ready;
            case "initializing":
                return Initializing;
            case "noIndex":
                return NoIndex;
            case "indexing":
                return Indexing;
            case "locked (upgrade to Pro to unlock)":
                return Locked;
            default:
                LOG.warn(String.format("Unknown file status value: '%s'", status));
                return Unknown;
        }
    }
}

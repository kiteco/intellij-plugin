package com.kite.intellij;

import java.time.Duration;

public interface KiteConstants {
    /**
     * The application id used on Mac OS X to identify the Kite cloud application.
     */
    String KITE_MAC_CLOUD_APPLICATION_ID = "com.kite.Kite";
    /**
     * The application id used on Mac OS X to identify the Kite enterprise application.
     */
    String KITE_MAC_ENTERPRISE_APPLICATION_ID = "enterprise.kite.Kite";

    /**
     * The maximum size a supported file may have to be send to the kite daemon.
     * <p>
     * This is 1 megabyte.
     */
    long MAX_FILE_SIZE_BYTES_FALLBACK = 1024 * 1024;

    /**
     * The source id used by the service implementations to talk to kite.
     */
    String APPLICATION_ID = "intellij";

    String ROLLBAR_TOKEN_PROD = "cee355fdf6ed4db793ec29954da9a0e3";

    String ROLLBAR_PLATFORM = "client";

    String KITE_HELP_URL = "https://help.kite.com/category/45-intellij-pycharm-integration";

    /**
     * The host used for connections to the Kite daemon.
     */
    String DEFAULT_HOST = "localhost";

    /**
     * The http port used for requests to {@link #DEFAULT_HOST}
     */
    int DEFAULT_PORT = 46624;

    /**
     * The possible widths for the signature info panel.
     */
    int[] SIGNATURE_POPUP_PREFERRED_WIDTH_PIXELS = new int[]{150, 175, 200, 225, 250, 275, 300, 250, 450};

    /**
     * Minimum height of the signature info panel.
     */
    int SIGNATURE_POPUP_MIN_HEIGHT_PIXELS = 50;

    /**
     * Maximum height in pixels of the signature info panel.
     */
    int SIGNATURE_POPUP_MAX_HEIGHT_PIXELS = 400;

    /**
     * The delay until a refresh of a currently opened signature info panel is triggered
     * after a caret move or an edit event.
     */
    int SIGNATURE_UPDATE_DELAY_MILLIS = 250;

    // ==== Timeouts ====

    /**
     * The queue timeout in milliseconds. The queue will process the queued items after waiting the time specified in this constant.
     */
    int DEFAULT_QUEUE_TIMEOUT_MILLIS = 100;

    /**
     * Milliseconds to wait for an empty queue if runWhenEmpty is called.
     */
    int EMPTY_QUEUE_TIMEOUT = 120;

    /**
     * Consecutive editor events will cancel previous events if they occur in this interval.
     */
    int ALARM_DELAY_MILLIS = 80;

    /**
     * Milliseconds to wait on a completed code completion response. After each wait the completion provider
     * checks whether the completion action has been cancelled by IntelliJ.
     */
    int COMPLETION_WAIT_TIMESLICE_MILLIS = 25;

    /**
     * Milliseconds to wait for an empty queue when a completion call is requested.
     * <p>
     * Kite's timeout is 100ms for completion requests, so we use a little more. If a request
     * takes longer than this duration then there's something wrong with the connection, probably.
     */
    int COMPLETION_CALL_QUEUE_TIMEOUT_MILLIS = 120;

    /**
     * Socket timeout for HTTP connections for very fast connection checks.
     */
    int SO_TIMEOUT_MILLIS_MINIMAL = 100;

    /**
     * Socket timeout for HTTP connections for quick connections.
     */
    int SO_TIMEOUT_MILLIS_SHORT = 300;

    /**
     * Socket itmeout for HTTP connections for normal connections.
     */
    int SO_TIMEOUT_MILLIS_DEFAULT = 1_000;

    /**
     * Socket timeout for HTTP connections for long-lived connections.
     */
    int SO_TIMEOUT_MILLIS_LONG = 10_000;

    /**
     * Duration after which the balloon notifications are faded out.
     */
    Duration NOTIFICATION_DEFAULT_FADEOUT = Duration.ofSeconds(3);

    int KITE_STATUS_UPDATE_INTERVAL = 10;
}

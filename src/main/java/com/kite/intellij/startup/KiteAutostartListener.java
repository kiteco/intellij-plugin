package com.kite.intellij.startup;

import com.intellij.util.messages.Topic;

public interface KiteAutostartListener {
    Topic<KiteAutostartListener> TOPIC = Topic.create("kite.autostart", KiteAutostartListener.class);

    /**
     * Is invoked after kited was automatically started at the initial startup of the IDE.
     */
    void onKiteAutostart();
}

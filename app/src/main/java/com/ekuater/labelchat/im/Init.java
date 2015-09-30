
package com.ekuater.labelchat.im;

import com.ekuater.labelchat.util.L;

public final class Init {

    private static final String TAG = "IM_Init";

    private static final String[] PRELOAD_CLASSES = {
            "com.ekuater.labelchat.im.Connection",
            "com.ekuater.labelchat.im.ReconnectionManager",
    };

    static {
        for (String s : PRELOAD_CLASSES) {
            try {
                Class.forName(s);
            } catch (ClassNotFoundException ex) {
                L.e(TAG, "Could not preload class for phone policy: " + s);
            }
        }
    }

    public static void init() {
    }
}

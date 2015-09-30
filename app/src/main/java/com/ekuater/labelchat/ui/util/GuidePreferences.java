package com.ekuater.labelchat.ui.util;

import android.content.Context;

/**
 * Created by Leo on 2014/12/25.
 *
 * @author LinYong
 */
public class GuidePreferences {

    private static final String PREFERENCE_NAME = "guide_preference";

    public static boolean isGuided(Context context, String key) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
                .getBoolean(key, false);
    }

    public static void setGuided(Context context, String key) {
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(key, true).commit();
    }
}

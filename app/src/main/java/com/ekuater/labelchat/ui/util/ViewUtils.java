package com.ekuater.labelchat.ui.util;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Leo on 2015/4/13.
 *
 * @author LinYong
 */
public final class ViewUtils {

    public static boolean touchEventInView(View view, MotionEvent ev) {
        int[] loc = new int[2];
        Rect rect;

        view.getLocationInWindow(loc);
        rect = new Rect(0, 0, view.getWidth(), view.getHeight());
        rect.offsetTo(loc[0], loc[1]);

        return rect.contains(round(ev.getRawX()), round(ev.getRawY()));
    }

    private static int round(float f) {
        return Math.round(f);
    }
}

package com.ekuater.labelchat.ui.util;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

/**
 * Created by Leo on 2015/3/17.
 *
 * @author LinYong
 */
public final class CompatUtils {

    @SuppressWarnings("deprecation")
    public static void setBackground(View view, Drawable background) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(background);
        } else {
            view.setBackground(background);
        }
    }
}

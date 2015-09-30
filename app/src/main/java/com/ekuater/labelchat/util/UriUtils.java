package com.ekuater.labelchat.util;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.net.Uri;

/**
 * Created by Leo on 2015/4/3.
 *
 * @author LinYong
 */
public class UriUtils {

    public static Uri getResourceUri(Resources res, int resId) {
        return new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(res.getResourcePackageName(resId))
                .path(res.getResourceTypeName(resId))
                .appendPath(res.getResourceEntryName(resId))
                .build();
    }
}

package com.ekuater.labelchat.ui.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.File;

/**
 * @author LinYong
 */
public final class AvatarFactory {

    public static Bitmap decodeAvatar(String path, int maxSize) {
        File file = new File(path);

        if (!file.exists() || !file.isFile()) {
            return null;
        }

        return decodeAvatarFile(path, maxSize);
    }

    public static Drawable decodeAvatarDrawable(String path, int maxSize) {
        Bitmap bitmap = decodeAvatar(path, maxSize);
        return (bitmap != null) ? new BitmapDrawable(null, bitmap) : null;
    }

    private static Bitmap decodeAvatarFile(String path, int maxSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap;

        options.inPreferredConfig = Bitmap.Config.RGB_565;

        // get image real size
        options.inJustDecodeBounds = true;
        bitmap = BitmapFactory.decodeFile(path, options);
        float realWidth = options.outWidth;
        float realHeight = options.outHeight;

        // decode the image now
        if (realHeight > 0 && realWidth > 0) {
            int scale = (int) (Math.max(realWidth / maxSize,
                    realHeight / maxSize) + 0.5F);
            options.inSampleSize = (scale >= 1) ? scale : 1;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(path, options);
        }

        return bitmap;
    }
}

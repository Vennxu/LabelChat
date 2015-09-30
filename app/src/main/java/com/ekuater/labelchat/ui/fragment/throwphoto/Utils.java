package com.ekuater.labelchat.ui.fragment.throwphoto;

import android.widget.ImageView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.delegate.ThrowPhotoManager;

/**
 * Created by Leo on 2015/1/8.
 *
 * @author LinYong
 */
public final class Utils {

    public static void showDisplayPhoto(ThrowPhotoManager manager, String url,
                                        ImageView imageView) {
        showDisplayPhoto(manager, url, imageView, R.drawable.pic_loading);
    }

    public static void showDisplayPhoto(ThrowPhotoManager manager, String url,
                                        ImageView imageView, int defaultResId) {
        manager.displayPhotoBitmap(url, imageView, defaultResId);
    }
}

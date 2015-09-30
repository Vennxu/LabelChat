package com.ekuater.labelchat.delegate.imageloader;

import android.graphics.Bitmap;

/**
 * Created by Leo on 2015/1/8.
 *
 * @author LinYong
 */
public interface OnlineImageLoadListener {

    public void onLoadFailed(String imageUri, LoadFailType loadFailType);

    public void onLoadComplete(String imageUri, Bitmap loadedImage);
}

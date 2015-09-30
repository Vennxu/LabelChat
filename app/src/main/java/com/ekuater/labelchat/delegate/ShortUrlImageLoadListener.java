package com.ekuater.labelchat.delegate;

import android.graphics.Bitmap;

import com.ekuater.labelchat.delegate.imageloader.LoadFailType;

/**
 * Created by Leo on 2015/2/4.
 *
 * @author LinYong
 */
public interface ShortUrlImageLoadListener {

    public void onLoadFailed(String shortUrl, LoadFailType loadFailType);

    public void onLoadComplete(String shortUrl, Bitmap loadedImage);
}

package com.ekuater.labelchat.delegate;

import android.graphics.Bitmap;

import com.ekuater.labelchat.delegate.imageloader.LoadFailType;
import com.ekuater.labelchat.delegate.imageloader.OnlineImageLoadListener;

/**
 * Created by Leo on 2015/2/4.
 *
 * @author LinYong
 */
/*package*/ class ShortUrlImageLoadWrapper implements OnlineImageLoadListener {

    private final String shortUrl;
    private final ShortUrlImageLoadListener listener;

    public ShortUrlImageLoadWrapper(String shortUrl, ShortUrlImageLoadListener listener) {
        this.shortUrl = shortUrl;
        this.listener = listener;
    }

    @Override
    public void onLoadFailed(String imageUri, LoadFailType loadFailType) {
        if (listener != null) {
            listener.onLoadFailed(shortUrl, loadFailType);
        }
    }

    @Override
    public void onLoadComplete(String imageUri, Bitmap loadedImage) {
        if (listener != null) {
            listener.onLoadComplete(shortUrl, loadedImage);
        }
    }
}

package com.ekuater.labelchat.ui.fragment.throwphoto;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.delegate.ShortUrlImageLoadListener;
import com.ekuater.labelchat.delegate.imageloader.LoadFailType;

import java.lang.ref.WeakReference;

/**
 * Created by Leo on 2015/1/8.
 *
 * @author LinYong
 */
public class PhotoLoadListener implements ShortUrlImageLoadListener {

    private static final int NO_ICON = 0;

    private final WeakReference<ImageView> mPhotoImageRef;
    private final int mFailResId;

    public PhotoLoadListener(String url, ImageView photoImage, int failResId) {
        mPhotoImageRef = new WeakReference<ImageView>(photoImage);
        mFailResId = failResId;
        setImageTag(photoImage, url);
    }

    public PhotoLoadListener(String url, ImageView photoImage) {
        this(url, photoImage, NO_ICON);
    }

    @Override
    public void onLoadFailed(String url, LoadFailType loadFailType) {
        if (mFailResId != NO_ICON) {
            final ImageView photoImage = mPhotoImageRef.get();
            if (photoImage != null && url.equals(photoImage.getTag(R.id.view_tag_first))) {
                photoImage.setImageResource(mFailResId);
                clearImageTag(photoImage);
            }
        }
    }

    @Override
    public void onLoadComplete(String url, Bitmap loadedImage) {
        final ImageView photoImage = mPhotoImageRef.get();
        if (photoImage != null && url.equals(photoImage.getTag(R.id.view_tag_first))) {
            photoImage.setImageBitmap(loadedImage);
            clearImageTag(photoImage);
        }
    }

    private void setImageTag(ImageView imageView, String url) {
        imageView.setTag(R.id.view_tag_first, url);
        imageView.setTag(R.id.view_tag_second, this);
    }

    private void clearImageTag(ImageView imageView) {
        imageView.setTag(R.id.view_tag_first, null);
        imageView.setTag(R.id.view_tag_second, null);
    }
}

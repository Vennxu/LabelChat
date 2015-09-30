package com.ekuater.labelchat.ui.util;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.delegate.ShortUrlImageLoadListener;
import com.ekuater.labelchat.delegate.imageloader.LoadFailType;

import java.lang.ref.WeakReference;

/**
 * @author LinYong
 */
public class AvatarLoadListener implements ShortUrlImageLoadListener {

    private static final int NO_ICON = 0;

    public final WeakReference<ImageView> mAvatarImageRef;
    public final int mFailResId;

    public AvatarLoadListener(String url, ImageView avatarImage, int failResId) {
        mAvatarImageRef = new WeakReference<ImageView>(avatarImage);
        mFailResId = failResId;
        setImageTag(avatarImage, url);
    }

    public AvatarLoadListener(String url, ImageView avatarImage) {
        this(url, avatarImage, NO_ICON);
    }

    @Override
    public void onLoadFailed(String url, LoadFailType loadFailType) {
        if (mFailResId != NO_ICON) {
            final ImageView avatarImage = mAvatarImageRef.get();
            if (avatarImage != null && url.equals(avatarImage.getTag(R.id.view_tag_first))) {
                avatarImage.setImageResource(mFailResId);
                clearImageTag(avatarImage);
            }
        }
    }

    @Override
    public void onLoadComplete(String url, Bitmap loadedImage) {
        final ImageView avatarImage = mAvatarImageRef.get();
        if (avatarImage != null && url.equals(avatarImage.getTag(R.id.view_tag_first))) {
            avatarImage.setImageBitmap(loadedImage);
            clearImageTag(avatarImage);
        }
    }

    public void setImageTag(ImageView imageView, String url) {
        imageView.setTag(R.id.view_tag_first, url);
        imageView.setTag(R.id.view_tag_second, this);
    }

    public void clearImageTag(ImageView imageView) {
        imageView.setTag(R.id.view_tag_first, null);
        imageView.setTag(R.id.view_tag_second, null);
    }
}

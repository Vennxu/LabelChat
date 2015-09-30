package com.ekuater.labelchat.ui.util;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ekuater.labelchat.R;

/**
 * Created by Label on 2015/1/24.
 *
 * @author XuWenxiang
 */
public class StoryImageLoadListener extends AvatarLoadListener {

    public StoryImageLoadListener(String url, ImageView imageView) {
        super(url, imageView);
    }

    @Override
    public void onLoadComplete(String url, Bitmap loadedImage) {
        ImageView imageView = this.mAvatarImageRef.get();
        if (imageView != null && url.equals(imageView.getTag(R.id.view_tag_first))) {
            imageView.post(new LoadCompleteRunnable(imageView, loadedImage));
        }
    }

    private void setImageBitmap(ImageView imageView, Bitmap bitmap) {
        ViewGroup.LayoutParams lp = imageView.getLayoutParams();

        if (lp != null) {
            float scale = (float) imageView.getMeasuredWidth() / bitmap.getWidth();
            float imageHeight = bitmap.getHeight() * scale;

            lp.height = (int) imageHeight;
            imageView.setLayoutParams(lp);
        }
        imageView.setImageBitmap(bitmap);
        clearImageTag(imageView);
    }

    private class LoadCompleteRunnable implements Runnable {

        private final ImageView imageView;
        private final Bitmap bitmap;

        public LoadCompleteRunnable(ImageView imageView, Bitmap bitmap) {
            this.imageView = imageView;
            this.bitmap = bitmap;
        }

        @Override
        public void run() {
            ViewGroup.LayoutParams lp = imageView.getLayoutParams();

            if (lp != null) {
                int measuredWidth = imageView.getMeasuredWidth();
                if (measuredWidth <= 0) {
                    imageView.addOnLayoutChangeListener(
                            new LayoutListener(imageView, bitmap));
                    imageView.requestLayout();
                    return;
                }
            }
            setImageBitmap(imageView, bitmap);
        }
    }

    private class LayoutListener implements View.OnLayoutChangeListener {

        private final ImageView imageView;
        private final Bitmap bitmap;

        public LayoutListener(ImageView imageView, Bitmap bitmap) {
            this.imageView = imageView;
            this.bitmap = bitmap;
        }

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                   int oldLeft, int oldTop, int oldRight, int oldBottom) {
            v.removeOnLayoutChangeListener(this);
            if (v == imageView) {
                setImageBitmap(imageView, bitmap);
            }
        }
    }
}

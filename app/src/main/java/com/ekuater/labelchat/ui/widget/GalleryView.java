package com.ekuater.labelchat.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

/**
 * @author FanChong
 */
public class GalleryView extends Gallery {
    public GalleryView(Context context) {
        super(context);
    }

    public GalleryView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public GalleryView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
    }


    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return super.onFling(e1, e2, velocityX/2, velocityY);
    }

}
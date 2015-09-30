package com.ekuater.labelchat.ui.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by Leo on 2015/3/7.
 *
 * @author LinYong
 */
public class DockerLayout extends FrameLayout {

    private GestureDetectorCompat mGestureDetector;
    private GestureDetector.OnGestureListener mGestureListener
            = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
            float offsetX = e2.getX() - e1.getX();

            if (offsetX >= 10) {
                translate(false);
                return true;
            } else if (offsetX <= -10) {
                translate(true);
                return true;
            } else {
                return false;
            }
        }
    };

    public DockerLayout(Context context) {
        super(context);
        init(context);
    }

    public DockerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DockerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mGestureDetector = new GestureDetectorCompat(context, mGestureListener);
        setLongClickable(true);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mGestureDetector.onTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        return super.onTouchEvent(ev);
    }

    private void translate(boolean show) {
        final int width = getWidth();
        final float fromX = getTranslationX();
        final float toX = show ? 0.0F : 0.8F * width;

        ObjectAnimator.ofFloat(this, "translationX", fromX, toX)
                .setDuration(200)
                .start();
    }
}

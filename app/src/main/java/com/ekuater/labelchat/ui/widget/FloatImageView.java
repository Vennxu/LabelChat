package com.ekuater.labelchat.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ImageView;

/**
 * A ImageView which can be moved by touch event in its parent ViewGroup
 *
 * @author LinYong
 */
public class FloatImageView extends ImageView {

    private int mTouchSlop;
    private boolean mMoving;
    private int mDownX;
    private int mDownY;
    private int mXDelta;
    private int mYDelta;

    public FloatImageView(Context context) {
        super(context);
        initialize(context);
    }

    public FloatImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public FloatImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    private void initialize(Context context) {
        mTouchSlop = ViewConfiguration.get(context).getScaledPagingTouchSlop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int x = (int) event.getRawX();
        final int y = (int) event.getRawY();
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        boolean handled = false;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownX = x;
                mDownY = y;
                mXDelta = x - getLeft();
                mYDelta = y - getTop();
                mMoving = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mMoving) {
                    final int dx = Math.abs(x - mDownX);
                    final int dy = Math.abs(y - mDownY);

                    if (dx > mTouchSlop || dy > mTouchSlop) {
                        mMoving = true;
                    }
                }

                if (mMoving) {
                    final int newX = x - mXDelta;
                    final int newY = y - mYDelta;
                    setFrame(newX, newY, newX + getWidth(), newY + getHeight());
                    handled = true;
                    setPressed(false);
                }
                break;
            case MotionEvent.ACTION_UP:
                handled = mMoving;
                mMoving = false;
                break;
            default:
                mMoving = false;
                break;
        }

        if (!handled) {
            super.onTouchEvent(event);
        }

        return true;
    }
}

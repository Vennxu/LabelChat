package com.ekuater.labelchat.ui.activity.chatting;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.Button;

/**
 * @author LinYong
 */
public class PressButton extends Button {

    public interface IPressListener {
        public void onPressChanged(boolean pressed, boolean cancel);
    }

    private boolean mPressState = false;
    private IPressListener mPressListener;

    public PressButton(Context context) {
        super(context);
    }

    public PressButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PressButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        // Do nothing, just ignore it
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            if (event.getAction() == MotionEvent.ACTION_UP && isPressed()) {
                setPressed(false);
            }
            return isClickable() || isLongClickable();
        }

        if (!isClickable() && !isLongClickable()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                setPressState(true);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int x = (int) event.getX();
                final int y = (int) event.getY();
                final int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

                if (!pointInView(x, y, touchSlop)) {
                    setPressState(false, true);
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                setPressState(false);
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                setPressState(false);
                break;
            }
            default:
                break;
        }

        return true;
    }

    public void setPressListener(IPressListener l) {
        mPressListener = l;
    }

    private boolean pointInView(float localX, float localY, float slop) {
        final int right = getRight();
        final int left = getLeft();
        final int bottom = getBottom();
        final int top = getTop();

        return localX >= -slop
                && localY >= -slop
                && localX < ((right - left) + slop)
                && localY < ((bottom - top) + slop);
    }

    private void setPressState(boolean pressed) {
        setPressState(pressed, false);
    }

    private void setPressState(boolean pressed, boolean cancel) {
        if (mPressState != pressed) {
            mPressState = pressed;
            setSelected(mPressState);
            notifyPressStateChanged(cancel);
        }
    }

    private void notifyPressStateChanged(boolean cancel) {
        if (mPressListener != null) {
            mPressListener.onPressChanged(mPressState, cancel);
        }
    }
}

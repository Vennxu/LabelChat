
package com.ekuater.labelchat.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.ekuater.labelchat.R;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

public class StrangerFriendGridView extends GridView {
    private int mSlop;
    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;
    protected long mAnimationTime = 150;
    private boolean mSwiping;
    private VelocityTracker mVelocityTracker;
    private int mDownPosition;
    private View mDownView;
    private float mDownX;
    private float mDownY;
    private int mViewWidth;
    private OnDismissCallback onDismissCallback;
    private boolean mLine = false;
    private boolean isMove;

    public void setLine(boolean isLine) {
        this.mLine = isLine;
    }

    public boolean getLine() {
        return mLine;
    }

    public boolean isMove() {
        return isMove;
    }

    public void setMove(boolean isMove) {
        this.isMove = isMove;
    }

    public void setmAnimationTime(long mAnimationTime) {
        this.mAnimationTime = mAnimationTime;
    }

    public void setOnDismissCallback(OnDismissCallback onDismissCallback) {
        this.onDismissCallback = onDismissCallback;
    }

    public StrangerFriendGridView(Context context) {
        this(context, null);
    }

    public StrangerFriendGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StrangerFriendGridView(Context context, AttributeSet attrs,
                                  int defStyle) {
        super(context, attrs, defStyle);

        ViewConfiguration vc = ViewConfiguration.get(context);
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 8;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(getContext().getResources().getColor(R.color.divider_color));
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View cellView = getChildAt(i);
            if (getLine() == true) {
                canvas.drawLine(cellView.getLeft(), cellView.getBottom(), cellView.getRight() * 3, cellView.getBottom(), paint);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                if (isMove) {
                    return handleActionMove(ev);
                } else {
                    return super.onTouchEvent(ev);
                }
            case MotionEvent.ACTION_UP:
                handleActionUp(ev);
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void handleActionDown(MotionEvent ev) {
        mDownX = ev.getX();
        mDownY = ev.getY();

        mDownPosition = pointToPosition((int) mDownX, (int) mDownY);

        if (mDownPosition == AdapterView.INVALID_POSITION) {
            return;
        }

        mDownView = getChildAt(mDownPosition - getFirstVisiblePosition());

        if (mDownView != null) {
            mViewWidth = mDownView.getWidth();
        }

        mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(ev);
    }

    @SuppressLint("Recycle")
    private boolean handleActionMove(MotionEvent ev) {
        if (mVelocityTracker == null || mDownView == null) {
            return super.onTouchEvent(ev);
        }
        float deltaX = ev.getX() - mDownX;
        float deltaY = ev.getY() - mDownY;

        if (Math.abs(deltaX) > mSlop && Math.abs(deltaY) < mSlop) {
            mSwiping = true;

            MotionEvent cancelEvent = MotionEvent.obtain(ev);
            cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                    (ev.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
            onTouchEvent(cancelEvent);
        }

        if (mSwiping) {
            ViewHelper.setTranslationX(mDownView, deltaX);
            return true;
        }

        return super.onTouchEvent(ev);

    }

    private void handleActionUp(MotionEvent ev) {
        if (mVelocityTracker == null || mDownView == null || !mSwiping) {
            return;
        }

        float deltaX = ev.getX() - mDownX;

        mVelocityTracker.computeCurrentVelocity(1000);
        float velocityX = Math.abs(mVelocityTracker.getXVelocity());
        float velocityY = Math.abs(mVelocityTracker.getYVelocity());

        boolean dismiss = false;
        boolean dismissRight = false;

        if (Math.abs(deltaX) > (mViewWidth / 2)) {
            dismiss = true;
            dismissRight = deltaX > 0;

        } else if (mMinFlingVelocity <= velocityX
                && velocityX <= mMaxFlingVelocity && velocityY < velocityX) {
            dismiss = true;
            dismissRight = mVelocityTracker.getXVelocity() > 0;
        }

        if (dismiss) {
            ViewPropertyAnimator.animate(mDownView)
                    .translationX(dismissRight ? mViewWidth : -mViewWidth)
                    .alpha(0)
                    .setDuration(mAnimationTime)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            performDismiss(mDownView, mDownPosition);
                        }
                    });
        } else {
            ViewPropertyAnimator.animate(mDownView)
                    .translationX(0)
                    .alpha(1)
                    .setDuration(mAnimationTime).setListener(null);
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }

        mSwiping = false;
    }

    private void performDismiss(final View dismissView, final int dismissPosition) {
        final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
        final int originalHeight = dismissView.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0).setDuration(mAnimationTime);
        animator.start();

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onDismissCallback != null) {
                    onDismissCallback.onDismiss(dismissPosition);
                }

                ViewHelper.setAlpha(dismissView, 1f);
                ViewHelper.setTranslationX(dismissView, 0);
                ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
                lp.height = originalHeight;
                dismissView.setLayoutParams(lp);

            }
        });
    }

    public interface OnDismissCallback {
        public void onDismiss(int dismissPosition);
    }

}

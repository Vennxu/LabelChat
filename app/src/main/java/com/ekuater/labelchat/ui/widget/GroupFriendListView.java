
package com.ekuater.labelchat.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Scroller;

public class GroupFriendListView extends ListView {
    private int slidePosition;
    private int downY;
    private int downX;
    private int screenWidth;
    private View itemView;
    private Scroller scroller;
    private static final int SNAP_VELOCITY = 600;
    private VelocityTracker velocityTracker;
    private boolean isSlide = false;
    private int mTouchSlop;
    private OnRemoveListener mOnRemoveListener;
    private RemoveDirection removeDirection;

    public enum RemoveDirection {
        RIGHT, LEFT;
    }

    public GroupFriendListView(Context context) {
        this(context, null);
    }

    public GroupFriendListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GroupFriendListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        screenWidth = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getWidth();
        scroller = new Scroller(context);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public void setOnRemoveListener(OnRemoveListener onRemoveListener) {
        this.mOnRemoveListener = onRemoveListener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                addVelocityTracker(event);

                if (!scroller.isFinished()) {
                    return super.dispatchTouchEvent(event);
                }
                downX = (int) event.getX();
                downY = (int) event.getY();

                slidePosition = pointToPosition(downX, downY);

                if (slidePosition == AdapterView.INVALID_POSITION) {
                    return super.dispatchTouchEvent(event);
                }

                itemView = getChildAt(slidePosition - getFirstVisiblePosition());
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (Math.abs(getScrollVelocity()) > SNAP_VELOCITY
                        || (Math.abs(event.getX() - downX) > mTouchSlop && Math
                                .abs(event.getY() - downY) < mTouchSlop)) {
                    isSlide = true;

                }
                break;
            }
            case MotionEvent.ACTION_UP:
                recycleVelocityTracker();
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    private void scrollRight() {
        removeDirection = RemoveDirection.RIGHT;
        final int delta = (screenWidth + itemView.getScrollX());
        scroller.startScroll(itemView.getScrollX(), 0, -delta, 0,
                Math.abs(delta));
        postInvalidate();
    }

    private void scrollLeft() {
        removeDirection = RemoveDirection.LEFT;
        final int delta = (screenWidth - itemView.getScrollX());
        scroller.startScroll(itemView.getScrollX(), 0, delta, 0,
                Math.abs(delta));
        postInvalidate();
    }

    private void scrollByDistanceX() {
        if (itemView.getScrollX() >= screenWidth / 4) {
            scrollLeft();
        } else if (itemView.getScrollX() <= -screenWidth / 4) {
            scrollRight();
        } else {
            itemView.scrollTo(0, 0);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isSlide && slidePosition != AdapterView.INVALID_POSITION) {
            requestDisallowInterceptTouchEvent(true);
            addVelocityTracker(ev);
            final int action = ev.getAction();
            int x = (int) ev.getX();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:

                    MotionEvent cancelEvent = MotionEvent.obtain(ev);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (ev.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    onTouchEvent(cancelEvent);

                    int deltaX = downX - x;
                    downX = x;

                    itemView.scrollBy(deltaX, 0);

                    return true;
                case MotionEvent.ACTION_UP:
                    int velocityX = getScrollVelocity();
                    if (velocityX > SNAP_VELOCITY) {
                        scrollRight();
                    } else if (velocityX < -SNAP_VELOCITY) {
                        scrollLeft();
                    } else {
                        scrollByDistanceX();
                    }

                    recycleVelocityTracker();
                    isSlide = false;
                    break;
            }
        }

        return super.onTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            itemView.scrollTo(scroller.getCurrX(), scroller.getCurrY());

            postInvalidate();

            if (scroller.isFinished()) {
                if (mOnRemoveListener == null) {
                    throw new NullPointerException(
                            "RemoveListener is null, we should called setRemoveListener()");
                }

                itemView.scrollTo(0, 0);
                mOnRemoveListener.removeItem(slidePosition);
            }
        }
    }

    private void addVelocityTracker(MotionEvent event) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }

        velocityTracker.addMovement(event);
    }

    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private int getScrollVelocity() {
        velocityTracker.computeCurrentVelocity(800);
        int velocity = (int) velocityTracker.getXVelocity();
        return velocity;
    }

    public interface OnRemoveListener {
        public void removeItem(int position);
    }

}

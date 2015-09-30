package com.ekuater.labelchat.ui.fragment.get;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.KeyEventCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;


public class CustomViewPager extends ViewPager {

    private final Rect mTempRect = new Rect();
    private PagerAdapter mAdapter;
    public static final String TAG = CustomViewPager.class.getSimpleName();

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public CustomViewPager(Context context) {
        super(context);

    }
    float x;
    float mLastMotionX;
    @Override
    public void setAdapter(PagerAdapter adapter) {
        mAdapter = adapter;
        super.setAdapter(mAdapter);
    }
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        // TODO Auto-generated method stub
//        switch (ev.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                Log.d("left","ACTION_DOWN...........");
//                x = ev.getX();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                Log.d("left","ACTION_MOVE...........");
//                mLastMotionX = ev.getX() - x;
//                if (mLastMotionX<0){
//                   return false;
//                }
//                break;
//            case MotionEvent.ACTION_CANCEL:
//                break;
//        }
//        return super.dispatchTouchEvent(ev);
//    }
//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//        // Let the focused view and/or our descendants get the key first
//        Log.d("left","left...........");
//        return super.dispatchKeyEvent(event) || executeKeyEvent(event);
//    }
//
//    /**
//     * You can call this function yourself to have the scroll view perform
//     * scrolling from a key event, just as if the event had been dispatched to
//     * it by the view hierarchy.
//     *
//     * @param event The key event to execute.
//     * @return Return true if the event was handled, else false.
//     */
//    @Override
//    public boolean executeKeyEvent(KeyEvent event) {
//        Log.d("left","left...........");
//        boolean handled = false;
//        if (event.getAction() == KeyEvent.ACTION_DOWN) {
//            switch (event.getKeyCode()) {
//                case KeyEvent.KEYCODE_DPAD_LEFT:
//                    handled = arrowScroll(FOCUS_LEFT);
//                    break;
//                case KeyEvent.KEYCODE_DPAD_RIGHT:
//                    handled = arrowScroll(FOCUS_RIGHT);
//                    break;
//                case KeyEvent.KEYCODE_TAB:
//                    if (Build.VERSION.SDK_INT >= 11) {
//                        // The focus finder had a bug handling FOCUS_FORWARD and FOCUS_BACKWARD
//                        // before Android 3.0. Ignore the tab key on those devices.
//                        if (KeyEventCompat.hasNoModifiers(event)) {
//                            handled = arrowScroll(FOCUS_FORWARD);
//                        } else if (KeyEventCompat.hasModifiers(event, KeyEvent.META_SHIFT_ON)) {
//                            handled = arrowScroll(FOCUS_BACKWARD);
//                        }
//                    }
//                    break;
//            }
//        }
//        return handled;
//    }
//
//    @Override
//    public boolean arrowScroll(int direction) {
//        View currentFocused = findFocus();
//        if (currentFocused == this) {
//            currentFocused = null;
//        } else if (currentFocused != null) {
//            boolean isChild = false;
//            for (ViewParent parent = currentFocused.getParent(); parent instanceof ViewGroup;
//                 parent = parent.getParent()) {
//                if (parent == this) {
//                    isChild = true;
//                    break;
//                }
//            }
//            if (!isChild) {
//                // This would cause the focus search down below to fail in fun ways.
//                final StringBuilder sb = new StringBuilder();
//                sb.append(currentFocused.getClass().getSimpleName());
//                for (ViewParent parent = currentFocused.getParent(); parent instanceof ViewGroup;
//                     parent = parent.getParent()) {
//                    sb.append(" => ").append(parent.getClass().getSimpleName());
//                }
//                Log.e(TAG, "arrowScroll tried to find focus based on non-child " +
//                        "current focused view " + sb.toString());
//                currentFocused = null;
//            }
//        }
//
//        boolean handled = false;
//
//        View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused,
//                direction);
//        if (nextFocused != null && nextFocused != currentFocused) {
//            if (direction == View.FOCUS_LEFT) {
//                // If there is nothing to the left, or this is causing us to
//                // jump to the right, then what we really want to do is page left.
//                final int nextLeft = getChildRectInPagerCoordinates(mTempRect, nextFocused).left;
//                final int currLeft = getChildRectInPagerCoordinates(mTempRect, currentFocused).left;
//                if (currentFocused != null && nextLeft >= currLeft) {
//                    handled = pageLeft();
//                } else {
//                    handled = nextFocused.requestFocus();
//                }
//            } else if (direction == View.FOCUS_RIGHT) {
//                // If there is nothing to the right, or this is causing us to
//                // jump to the left, then what we really want to do is page right.
//                final int nextLeft = getChildRectInPagerCoordinates(mTempRect, nextFocused).left;
//                final int currLeft = getChildRectInPagerCoordinates(mTempRect, currentFocused).left;
//                if (currentFocused != null && nextLeft <= currLeft) {
//                    handled = pageRight();
//                } else {
//                    handled = nextFocused.requestFocus();
//                }
//            }
//        } else if (direction == FOCUS_LEFT || direction == FOCUS_BACKWARD) {
//            // Trying to move left and nothing there; try to page.
//            handled = pageLeft();
//        } else if (direction == FOCUS_RIGHT || direction == FOCUS_FORWARD) {
//            // Trying to move right and nothing there; try to page.
//            handled = pageRight();
//        }
//        if (handled) {
//            playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
//        }
//        return handled;
//    }
//
//
//    private Rect getChildRectInPagerCoordinates(Rect outRect, View child) {
//        if (outRect == null) {
//            outRect = new Rect();
//        }
//        if (child == null) {
//            outRect.set(0, 0, 0, 0);
//            return outRect;
//        }
//        outRect.left = child.getLeft();
//        outRect.right = child.getRight();
//        outRect.top = child.getTop();
//        outRect.bottom = child.getBottom();
//
//        ViewParent parent = child.getParent();
//        while (parent instanceof ViewGroup && parent != this) {
//            final ViewGroup group = (ViewGroup) parent;
//            outRect.left += group.getLeft();
//            outRect.right += group.getRight();
//            outRect.top += group.getTop();
//            outRect.bottom += group.getBottom();
//
//            parent = group.getParent();
//        }
//        return outRect;
//    }
//
//    boolean pageLeft() {
////        if (getCurrentItem() > 0) {
////            setCurrentItem(getCurrentItem() - 1, true);
////            return true;
////        }
//        return false;
//    }
//
//    boolean pageRight() {
//        if (mAdapter != null && getCurrentItem() < (mAdapter.getCount() - 1)) {
//            setCurrentItem(getCurrentItem() + 1, true);
//            return true;
//        }
//        return false;
//    }

//    @Override
//    public boolean performAccessibilityAction(int action, Bundle arguments) {
////        return super.performAccessibilityAction(action, arguments);
////        if (super.performAccessibilityAction(host, action, args)) {
////            return true;
////        }
//        switch (action) {
//            case AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD: {
//                if (canScrollHorizontally(1)) {
////                    setCurrentItem(getCurrentItem() + 1);
//                    return false;
//                }
//            } return false;
//            case AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD: {
//                if (canScrollHorizontally(-1)) {
//                    setCurrentItem(getCurrentItem() - 1);
//                    return true;
//                }
//            } return false;
//        }
//        return false;
//    }



}
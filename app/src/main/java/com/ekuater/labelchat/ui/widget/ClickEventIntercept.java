package com.ekuater.labelchat.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by Administrator on 2015/2/7.
 *
 * @author Fan Chong
 */
public class ClickEventIntercept extends RelativeLayout {

    public ClickEventIntercept(Context context) {
        super(context);
    }

    public ClickEventIntercept(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClickEventIntercept(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
}

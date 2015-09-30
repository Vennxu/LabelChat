package com.ekuater.labelchat.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * @author LinYong
 */
public class LabelView extends TextView {

    private static final int[] CHECKED_STATE_SET = {
            android.R.attr.state_checked,
    };

    private boolean mOwned = false;

    public LabelView(Context context) {
        super(context);
    }

    public LabelView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LabelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOwned(boolean owned) {
        if (owned != mOwned) {
            mOwned = owned;
            refreshDrawableState();
        }
    }

    public boolean isOwned() {
        return mOwned;
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState;

        if (isOwned()) {
            drawableState = super.onCreateDrawableState(extraSpace + 1);
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        } else {
            drawableState = super.onCreateDrawableState(extraSpace);
        }

        return drawableState;
    }
}

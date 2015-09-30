package com.ekuater.labelchat.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.ekuater.labelchat.R;

/**
 * @author LinYong
 */
public class MaxSizeScrollView extends ScrollView {

    private int mMaxWidth = Integer.MAX_VALUE;
    private int mMaxHeight = Integer.MAX_VALUE;

    public MaxSizeScrollView(Context context) {
        super(context);
        initView(context, null, 0);
    }

    public MaxSizeScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs, 0);
    }

    public MaxSizeScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context, attrs, defStyle);
    }

    private void initView(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.MaxSizeScrollView, defStyle, 0);
        mMaxHeight = a.getDimensionPixelSize(
                R.styleable.MaxSizeScrollView_maxHeight, mMaxHeight);
        mMaxWidth = a.getDimensionPixelSize(
                R.styleable.MaxSizeScrollView_maxWidth, mMaxWidth);
        a.recycle();
    }

    public void setMaxWidth(int maxWidth) {
        mMaxWidth = maxWidth;
    }

    public void setMaxHeight(int maxHeight) {
        mMaxHeight = maxHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(
                resolveDesiredSize(getMeasuredWidthAndState(), mMaxWidth, widthMeasureSpec),
                resolveDesiredSize(getMeasuredHeightAndState(), mMaxHeight, heightMeasureSpec));
    }

    private int resolveDesiredSize(int measuredSize, int maxSize, int measureSpec) {
        final int size = MeasureSpec.getSize(measuredSize);
        final int mode = MeasureSpec.getMode(measuredSize);
        final int newSize = resolveAdjustedSize(size, maxSize, measureSpec);
        return resolveSizeAndState(newSize, mode, 0);
    }

    private int resolveAdjustedSize(int desiredSize, int maxSize, int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                /*
                 * Parent says we can be as big as we want. Just don't be larger
                 * than max size imposed on ourselves.
                 */
                result = Math.min(desiredSize, maxSize);
                break;
            case MeasureSpec.AT_MOST:
                // Parent says we can be as big as we want, up to specSize.
                // Don't be larger than specSize, and don't be larger than
                // the max size imposed on ourselves.
                result = Math.min(Math.min(desiredSize, specSize), maxSize);
                break;
            case MeasureSpec.EXACTLY:
                // No choice. Do what we are told.
                result = specSize;
                break;
        }
        return result;
    }
}

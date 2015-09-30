package com.ekuater.labelchat.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RatingBar;

import com.ekuater.labelchat.R;

/**
 * Created by Leo on 2015/2/12.
 *
 * @author LinYong
 */
public class WrapRatingBar extends RatingBar {

    private Drawable mSampleTileDrawable = null;

    public WrapRatingBar(Context context) {
        super(context);
        init(context, null, 0);
    }

    public WrapRatingBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public WrapRatingBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.WrapRatingBar, defStyle, 0);
        mSampleTileDrawable = a.getDrawable(R.styleable.WrapRatingBar_sampleTile);
        a.recycle();
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mSampleTileDrawable != null) {
            int numStars = getNumStars();
            int width = mSampleTileDrawable.getIntrinsicWidth() * numStars;
            int height = mSampleTileDrawable.getIntrinsicHeight();
            setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, 0),
                    resolveSizeAndState(height, heightMeasureSpec, 0));
        }
    }
}

package com.ekuater.labelchat.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.ekuater.labelchat.R;

public class FlowLayout extends ViewGroup {

    private int mHorizontalGap = 0;
    private int mVerticalGap = 0;
    private int mMaxLines = -1;
    private Drawable mMoreDrawable = null;
    private Point mMoreDrawableSize = null;
    private Rect mMoreDrawableRect = null;
    private int mLastVisibleChildIdx = -1;

    public FlowLayout(Context context) {
        super(context);
        initFromAttributes(context, null);
    }

    public FlowLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initFromAttributes(context, attributeSet);
    }

    public FlowLayout(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        initFromAttributes(context, attributeSet);
    }

    private void initFromAttributes(Context context, AttributeSet attributeSet) {
        TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.FlowLayout);
        final int N = a.getIndexCount();

        mHorizontalGap = 0;
        mVerticalGap = 0;
        mMaxLines = -1;
        mMoreDrawable = null;

        for (int i = 0; i < N; i++) {
            final int attr = a.getIndex(i);

            switch (attr) {
                case R.styleable.FlowLayout_horizontalGap:
                    mHorizontalGap = a.getDimensionPixelSize(attr, mHorizontalGap);
                    break;
                case R.styleable.FlowLayout_verticalGap:
                    mVerticalGap = a.getDimensionPixelSize(attr, mVerticalGap);
                    break;
                case R.styleable.FlowLayout_maxLines:
                    mMaxLines = a.getInteger(attr, mMaxLines);
                    break;
                case R.styleable.FlowLayout_moreDrawable:
                    mMoreDrawable = a.getDrawable(attr);
                    break;
                default:
                    break;
            }
        }

        a.recycle();

        mMaxLines = (mMaxLines > 0) ? mMaxLines : -1;
        if (mMoreDrawable != null) {
            int w = mMoreDrawable.getIntrinsicWidth();
            int h = mMoreDrawable.getIntrinsicHeight();

            if (w > 0 && h > 0) {
                mMoreDrawableSize = new Point(w, h);
                mMoreDrawableRect = new Rect(0, 0, w, h);
            }
        }
    }

    public void setVerticalGap(int mVerticalGap) {
        this.mVerticalGap = mVerticalGap;
    }

    public void setHorizontalGap(int mHorizontalGap) {
        this.mHorizontalGap = mHorizontalGap;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureHorizontal(widthMeasureSpec, heightMeasureSpec);
    }

    private void measureHorizontal(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final boolean exactly = (widthMode == MeasureSpec.EXACTLY);
        final int vWidth = exactly ? widthSize : Integer.MAX_VALUE;

        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        final int paddingRight = getPaddingRight();
        final int paddingBottom = getPaddingBottom();
        final int scrollX = getScrollX();
        final int scrollY = getScrollY();

        final int count = getChildCount();
        int currentRow = 0;
        int currentColumn = 0;
        int currentRowWidth = 0;
        int currentColumnHeight = 0;
        int desiredWidth = 0;
        int desiredHeight = 0;
        int maxLineIdx = mMaxLines - 1;

        mLastVisibleChildIdx = -1;
        if (mMoreDrawableRect != null) {
            mMoreDrawableRect.set(0, 0, mMoreDrawableRect.width(),
                    mMoreDrawableRect.height());
        }

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);

            if (child == null || child.getVisibility() == GONE) {
                continue;
            }

            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            int horizontalGap = getHorizontalGap(lp);
            int verticalGap = getVerticalGap(lp);
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            int tmpCurrentRowWidth = currentRowWidth
                    + ((currentColumn == 0) ? 0 : horizontalGap) + childWidth;

            final boolean newRow = (lp.mNewLine || (exactly && tmpCurrentRowWidth > vWidth))
                    && (currentRow != 0 || currentColumn != 0);
            if (newRow) {
                if (currentRow == maxLineIdx) {
                    int x, y;

                    if ((currentRowWidth + mMoreDrawableSize.x) <= vWidth) {
                        mLastVisibleChildIdx = i - 1;
                        x = paddingLeft + currentRowWidth + horizontalGap;
                        y = paddingTop + desiredHeight + verticalGap;
                    } else {
                        mLastVisibleChildIdx = i - 2;
                        LayoutParams tmpLp = (LayoutParams) getChildAt(i - 1).getLayoutParams();
                        x = tmpLp.mLeft;
                        y = tmpLp.mTop;
                    }

                    if (mMoreDrawableRect != null) {
                        mMoreDrawableRect.offset(x,
                                y + (childHeight - mMoreDrawableRect.height()) / 2);
                    }
                    break;
                }

                desiredWidth = Math.max(currentRowWidth, desiredWidth);
                desiredHeight += currentColumnHeight;
                currentColumn = 0;
                currentRowWidth = childWidth;
                currentColumnHeight = 0;
                currentRow++;
            } else {
                currentRowWidth = tmpCurrentRowWidth;
            }
            int currentHeight = ((currentRow == 0) ? 0 : verticalGap) + childHeight;
            currentColumnHeight = Math.max(currentHeight, currentColumnHeight);
            currentColumn++;

            lp.mLeft = paddingLeft + (currentRowWidth - childWidth) + scrollX;
            lp.mTop = paddingTop + desiredHeight + (currentHeight - childHeight) + scrollY;
            lp.mRight = lp.mLeft + childWidth;
            lp.mBottom = lp.mTop + childHeight;
        }
        desiredWidth = Math.max(currentRowWidth, desiredWidth);
        desiredHeight += currentColumnHeight;

        int w = Math.max(desiredWidth, getSuggestedMinimumWidth())
                + paddingLeft + paddingRight;
        int h = Math.max(desiredHeight, getSuggestedMinimumHeight())
                + paddingTop + paddingBottom;
        setMeasuredDimension(resolveSizeAndState(w, widthMeasureSpec, 0),
                resolveSizeAndState(h, heightMeasureSpec, 0));
    }

    private int getVerticalGap(LayoutParams lp) {
        return lp.verticalGapSpecified() ? lp.mVerticalGap : mVerticalGap;
    }

    private int getHorizontalGap(LayoutParams lp) {
        return lp.horizontalGapSpecified() ? lp.mHorizontalGap : mHorizontalGap;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);

            if (mLastVisibleChildIdx > 0 && i == mLastVisibleChildIdx + 1) {
                break;
            }

            if (child.getVisibility() != GONE) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                child.layout(lp.mLeft, lp.mTop, lp.mRight, lp.mBottom);
            }
        }
    }

    @Override
    protected boolean drawChild(@NonNull Canvas canvas, @NonNull View child, long drawingTime) {
        boolean _ret = super.drawChild(canvas, child, drawingTime);

        if (mMoreDrawableRect != null && mLastVisibleChildIdx > 0) {
            mMoreDrawable.setBounds(mMoreDrawableRect);
            mMoreDrawable.draw(canvas);
        }

        return _ret;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {

        public static final int NO_GAP = -1;

        private int mHorizontalGap = NO_GAP;
        private int mVerticalGap = NO_GAP;
        private boolean mNewLine = false;
        private int mLeft, mTop, mRight, mBottom;

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            initFromAttributes(context, attributeSet);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
        }

        public boolean horizontalGapSpecified() {
            return mHorizontalGap != NO_GAP;
        }

        public boolean verticalGapSpecified() {
            return mVerticalGap != NO_GAP;
        }

        private void initFromAttributes(Context context, AttributeSet attributeSet) {
            TypedArray a = context.obtainStyledAttributes(attributeSet,
                    R.styleable.FlowLayout_LayoutParams);
            final int N = a.getIndexCount();

            mHorizontalGap = NO_GAP;
            mVerticalGap = NO_GAP;
            mNewLine = false;

            for (int i = 0; i < N; i++) {
                int attr = a.getIndex(i);

                switch (attr) {
                    case R.styleable.FlowLayout_LayoutParams_layout_horizontalGap:
                        mHorizontalGap = a.getDimensionPixelSize(attr, NO_GAP);
                        break;
                    case R.styleable.FlowLayout_LayoutParams_layout_verticalGap:
                        mVerticalGap = a.getDimensionPixelSize(attr, NO_GAP);
                        break;
                    case R.styleable.FlowLayout_LayoutParams_layout_newLine:
                        mNewLine = a.getBoolean(attr, false);
                        break;
                    default:
                        break;
                }
            }

            a.recycle();
        }
    }
}
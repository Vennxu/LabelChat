package com.ekuater.labelchat.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.CompoundButton;

import com.ekuater.labelchat.R;

/**
 * Created by Label on 2014/12/16.
 */
public class PieView extends CompoundButton {
    private int mMax;
    private int mProgress;
    private Drawable mShadowDrawable;
    private Paint mCirclePaint;
    private Paint mProgressPaint;
    private Rect mTempRect = new Rect();
    private RectF mTempRectF = new RectF();
    private int mDrawableSize;
    private int mInnerSize;

    public PieView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public PieView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context, attributeSet, 0);
    }

    public PieView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        init(context, attributeSet, defStyle);
    }

    private void init(Context context, AttributeSet attributeSet, int defStyle) {
        mMax = 100;
        mProgress = 0;
        final Resources res = getResources();
        int circleColor = res.getColor(R.color.gray);
        int progressColor = res.getColor(R.color.pieViewColor);
        if (attributeSet != null) {
            final TypedArray ta = context.obtainStyledAttributes(attributeSet, R.styleable.PieView, defStyle, 0);
            mMax = ta.getInteger(R.styleable.PieView_maxs, mMax);
            mProgress = ta.getInteger(R.styleable.PieView_progresss, mProgress);
            circleColor = ta.getColor(R.styleable.PieView_circleColor, circleColor);
            progressColor = ta.getColor(R.styleable.PieView_progresssColor, progressColor);
            ta.recycle();
        }
        mShadowDrawable = res.getDrawable(R.drawable.pie_progress_shadow);
        mShadowDrawable.setCallback(this);
        mDrawableSize = mShadowDrawable.getIntrinsicWidth();
        mInnerSize = getResources().getDimensionPixelSize(R.dimen.pie_progress_inner_size);
        mCirclePaint = new Paint();
        mCirclePaint.setColor(circleColor);
        mCirclePaint.setAntiAlias(true);
        mProgressPaint = new Paint();
        mProgressPaint.setColor(progressColor);
        mProgressPaint.setAntiAlias(true);
    }

    public int getMax() {
        return mMax;
    }

    public void setMax(int max) {
        if (max > 0) {
            mMax = max;
        }
        invalidate();
    }

    public int getProgress() {
        return mProgress;
    }


    public void setProgress(int progress) {
        mProgress = progress;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(resolveSize(mDrawableSize, widthMeasureSpec), resolveSize(mDrawableSize, heightMeasureSpec));
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mShadowDrawable.isStateful()) {
            mShadowDrawable.setState(getDrawableState());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mTempRect.set(0, 0, mDrawableSize, mDrawableSize);
        mTempRect.offset((getWidth() - mDrawableSize) / 2, (getHeight() - mDrawableSize) / 2);
        mTempRectF.set(-0.5f, -0.5f, mInnerSize + 0.5f, mInnerSize + 0.5f);
        mTempRectF.offset((getWidth() - mInnerSize) / 2, (getHeight() - mInnerSize) / 2);
        canvas.drawArc(mTempRectF, 0, 360, true, mCirclePaint);
        canvas.drawArc(mTempRectF, -90, -360 * mProgress / mMax, true, mProgressPaint);
        mShadowDrawable.setBounds(mTempRect);
        mShadowDrawable.draw(canvas);
    }

    public static class SavedState extends BaseSavedState {
        private int mProgress;
        private int mMax;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            mProgress = in.readInt();
            mMax = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mProgress);
            out.writeInt(mMax);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isSaveEnabled()) {
            SavedState ss = new SavedState(superState);
            ss.mMax = mMax;
            ss.mProgress = mProgress;
            return ss;
        }
        return superState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        mMax = ss.mMax;
        mProgress = ss.mProgress;
    }


}

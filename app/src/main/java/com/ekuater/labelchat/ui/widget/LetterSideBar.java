package com.ekuater.labelchat.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.ekuater.labelchat.R;

public class LetterSideBar extends View {

    public interface OnLetterChosenListener {
        public void onLetterChosen(String s);
    }

    private final String[] mLetters;

    private Layout[] mLetterLayouts;
    private int mChosenIdx = -1;
    private int mLetterTextSize;
    private int mNormalLetterColor;
    private int mChosenLetterColor;
    private int mDesiredWidth;
    private int mDesiredHeight;
    private TextPaint mLetterPaint = new TextPaint();
    private TextView mPromptView;
    private OnLetterChosenListener mLetterChosenListener;

    public LetterSideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mLetters = getInitLetters(context);
        initAttrs(context, attrs, defStyle);
    }

    public LetterSideBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LetterSideBar(Context context) {
        this(context, null);
    }

    private String[] getInitLetters(Context context) {
        String[] letters;

        if (isInEditMode()) {
            letters = new String[]{
                    "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                    "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
                    "U", "V", "W", "X", "Y", "Z",
            };
        } else {
            letters = context.getResources().getStringArray(R.array.side_bar_letter_array);
        }

        return letters;
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LetterSideBar,
                defStyle, 0);
        final int N = a.getIndexCount();

        mLetterTextSize = 20;
        mNormalLetterColor = Color.GRAY;
        mChosenLetterColor = Color.GREEN;

        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);

            switch (attr) {
                case R.styleable.LetterSideBar_letterTextSize:
                    mLetterTextSize = a.getDimensionPixelSize(attr, mLetterTextSize);
                    break;
                case R.styleable.LetterSideBar_normalLetterColor:
                    mNormalLetterColor = a.getColor(attr, mNormalLetterColor);
                    break;
                case R.styleable.LetterSideBar_chosenLetterColor:
                    mChosenLetterColor = a.getColor(attr, mChosenLetterColor);
                    break;
                default:
                    break;
            }
        }

        a.recycle();

        mLetterPaint.setColor(mChosenLetterColor);
        mLetterPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mLetterPaint.setAntiAlias(true);
        mLetterPaint.setTextSize(mLetterTextSize);
        mLetterPaint.setFakeBoldText(false);

        mDesiredWidth = 0;
        mDesiredHeight = 0;
        mLetterLayouts = new Layout[mLetters.length];
        for (int i = 0; i < mLetters.length; i++) {
            Layout layout = makeLayout(mLetters[i], mLetterPaint);
            mLetterLayouts[i] = layout;
            mDesiredWidth = Math.max(layout.getWidth(), mDesiredWidth);
            mDesiredHeight += layout.getHeight();
        }
    }

    public void setLetterChosenPromptView(TextView promptView) {
        mPromptView = promptView;
    }

    public void setOnLetterChosenListener(OnLetterChosenListener listener) {
        mLetterChosenListener = listener;
    }

    private Layout makeLayout(CharSequence text, TextPaint paint) {
        return new StaticLayout(text, paint,
                (int) Math.ceil(Layout.getDesiredWidth(text, paint)),
                Layout.Alignment.ALIGN_NORMAL, 1.f, 0, true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = Math.max(mDesiredWidth, getSuggestedMinimumWidth())
                + getPaddingLeft() + getPaddingRight();
        int h = Math.max(mDesiredHeight, getSuggestedMinimumHeight())
                + getPaddingTop() + getPaddingBottom();
        int widthSize = resolveSizeAndState(w, widthMeasureSpec, 0);
        int heightSize = resolveSizeAndState(h, heightMeasureSpec, 0);
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final Layout[] layouts = mLetterLayouts;
        final int length = layouts.length;

        final int right = getRight();
        final int left = getLeft();
        final int bottom = getBottom();
        final int top = getTop();
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        final int paddingRight = getPaddingRight();
        final int paddingBottom = getPaddingBottom();
        final int hspace = right - left - paddingLeft - paddingRight;
        final int vspace = bottom - top - paddingTop - paddingBottom;
        final int letterHeight = vspace / length;

        final int saveCount = canvas.getSaveCount();
        canvas.save();
        canvas.clipRect(paddingLeft, paddingTop, paddingLeft + hspace, paddingTop + vspace);

        int startX = paddingLeft;
        int startY = paddingTop;

        for (int i = 0; i < length; ++i) {
            Layout layout = layouts[i];

            if (i == mChosenIdx) {
                mLetterPaint.setColor(mChosenLetterColor);
                mLetterPaint.setFakeBoldText(true);
            } else {
                mLetterPaint.setColor(mNormalLetterColor);
                mLetterPaint.setFakeBoldText(false);
            }

            drawText(layout, canvas, startX + (hspace - layout.getWidth()) / 2,
                    startY + (letterHeight - layout.getHeight()) / 2);
            startY += letterHeight;
        }

        canvas.restoreToCount(saveCount);
    }

    private void drawText(Layout layout, Canvas canvas, int x, int y) {
        canvas.save();
        canvas.translate(x, y);
        layout.draw(canvas);
        canvas.restore();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();
        final OnLetterChosenListener listener = mLetterChosenListener;

        if (action == MotionEvent.ACTION_DOWN) {
            requestFocus();
        }

        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                setPressed(false);
                mChosenIdx = -1;
                if (mPromptView != null) {
                    mPromptView.setVisibility(View.GONE);
                }
                invalidate();
                break;
            }

            default: {
                final int paddingTop = getPaddingTop();
                final int paddingBottom = getPaddingBottom();
                final int vspace = getHeight() - paddingTop - paddingBottom;
                final int c = (y > (paddingTop + vspace) || y < paddingTop) ? -1
                        : (int) ((y - paddingTop) / vspace * mLetters.length);

                setPressed(true);
                if (mChosenIdx != c) {
                    if (c >= 0 && c < mLetters.length) {
                        if (listener != null) {
                            listener.onLetterChosen(mLetters[c]);
                        }
                        if (mPromptView != null) {
                            mPromptView.setText(mLetters[c]);
                            mPromptView.setVisibility(View.VISIBLE);
                        }

                        mChosenIdx = c;
                        invalidate();
                    }
                }
                break;
            }
        }

        return true;
    }
}
package com.ekuater.labelchat.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.UserLabel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * @author LinYong
 */
public class LabelShowView extends View {

    public interface IListener {

        public void onLabelClicked(UserLabel label);

        public void onEntrySelectMode();

        public void onExitSelectMode();

        public void onLabelSelected(UserLabel label, boolean selected);
    }

    // private static final String TAG = UserLabelShowView.class.getSimpleName();

    private static final int LABEL_SIDE_LEFT = 0;
    private static final int LABEL_SIDE_RIGHT = 1;

    private static final int MODE_NORMAL_MODE = 0;
    private static final int MODE_SELECT_MODE = 1;

    private static final class LabelItem implements Comparable<LabelItem> {

        public static final int STATE_NORMAL = 0;
        public static final int STATE_SELECTED = 1;

        private static final long DAY_TIME_IN_MILLIS = 86400000L;

        public final UserLabel mUserLabel;
        public final String mName;
        public final long mTime;
        public final Rect mShowBounds = new Rect();
        public int mSide = LABEL_SIDE_LEFT;
        public boolean mIsSideHeader = false;
        public int mOffsetY = 0;
        public int mState = STATE_NORMAL;

        public Layout mNameLayout = null;
        public Layout mMonthLayout = null;
        public Layout mDateLayout = null;

        public LabelItem(UserLabel userLabel) {
            mUserLabel = userLabel;
            mName = mUserLabel.getName();
            long time = mUserLabel.getTime();
            mTime = time - (time % DAY_TIME_IN_MILLIS);
        }

        public void setSelected(boolean selected) {
            mState = selected ? STATE_SELECTED : STATE_NORMAL;
        }

        public boolean getSelected() {
            return mState == STATE_SELECTED;
        }

        @Override
        public int compareTo(LabelItem another) {
            long diff = this.mTime - another.mTime;
            return (diff > 0) ? -1 : ((diff == 0) ? 0 : 1);
        }
    }

    private static abstract class ObjectRunnable implements Runnable {

        protected final Object mObject;

        public ObjectRunnable(Object object) {
            mObject = object;
        }
    }

    private List<LabelItem> mItemList = new ArrayList<LabelItem>();
    private final List<WeakReference<LabelItem>> mSelectItemList
            = new ArrayList<WeakReference<LabelItem>>();
    private WeakReference<IListener> mListener;

    private Drawable mLeftLabelLeafDrawable;
    private ColorStateList mLeftLabelLeafTextColor;
    private int mLeftLabelLeafTextSize;
    private Drawable mLeftLabelBranchDrawable;
    private int mLeftLabelBranchWidth;

    private Drawable mRightLabelLeafDrawable;
    private ColorStateList mRightLabelLeafTextColor;
    private int mRightLabelLeafTextSize;
    private Drawable mRightLabelBranchDrawable;
    private int mRightLabelBranchWidth;

    private int mSameDateLabelDist;

    private int mMonthTextSize;
    private int mMonthTextColor;
    private int mDateTextSize;
    private int mDateTextColor;
    private int mTimeShowGap;

    private Drawable mTimelineDotDrawable;
    private int mTimelineDotDist;

    private TextPaint mTextPaint;

    private Rect mLeftLabelLeafDrawablePadding = new Rect();
    private Rect mRightLabelLeafDrawablePadding = new Rect();
    private int mDesiredWidth = 0;
    private int mDesiredHeight = 0;

    private String mMonthString;
    private String mDateString;
    private int mMode = MODE_NORMAL_MODE;

    public LabelShowView(Context context) {
        this(context, null);
    }

    public LabelShowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LabelShowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs, defStyleAttr);
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LabelShowView,
                defStyleAttr, 0);
        final int N = a.getIndexCount();

        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);

            switch (attr) {
                case R.styleable.LabelShowView_leftLeafDrawable:
                    mLeftLabelLeafDrawable = a.getDrawable(attr);
                    break;
                case R.styleable.LabelShowView_leftLeafTextColor:
                    mLeftLabelLeafTextColor = a.getColorStateList(attr);
                    break;
                case R.styleable.LabelShowView_leftLeafTextSize:
                    mLeftLabelLeafTextSize = a.getDimensionPixelSize(attr, 15);
                    break;
                case R.styleable.LabelShowView_leftBranchDrawable:
                    mLeftLabelBranchDrawable = a.getDrawable(attr);
                    break;
                case R.styleable.LabelShowView_leftBranchWidth:
                    mLeftLabelBranchWidth = a.getDimensionPixelSize(attr, 0);
                    break;
                case R.styleable.LabelShowView_rightLeafDrawable:
                    mRightLabelLeafDrawable = a.getDrawable(attr);
                    break;
                case R.styleable.LabelShowView_rightLeafTextColor:
                    mRightLabelLeafTextColor = a.getColorStateList(attr);
                    break;
                case R.styleable.LabelShowView_rightLeafTextSize:
                    mRightLabelLeafTextSize = a.getDimensionPixelSize(attr, 15);
                    break;
                case R.styleable.LabelShowView_rightBranchDrawable:
                    mRightLabelBranchDrawable = a.getDrawable(attr);
                    break;
                case R.styleable.LabelShowView_rightBranchWidth:
                    mRightLabelBranchWidth = a.getDimensionPixelSize(attr, 0);
                    break;
                case R.styleable.LabelShowView_sameDateLabelDist:
                    mSameDateLabelDist = a.getDimensionPixelSize(attr, 3);
                    break;
                case R.styleable.LabelShowView_timeLineDotDrawable:
                    mTimelineDotDrawable = a.getDrawable(attr);
                    break;
                case R.styleable.LabelShowView_timeLineDotDist:
                    mTimelineDotDist = a.getDimensionPixelSize(attr, 10);
                    break;
                case R.styleable.LabelShowView_monthTextSize:
                    mMonthTextSize = a.getDimensionPixelSize(attr, 15);
                    break;
                case R.styleable.LabelShowView_monthTextColor:
                    mMonthTextColor = a.getColor(attr, Color.BLACK);
                    break;
                case R.styleable.LabelShowView_dateTextSize:
                    mDateTextSize = a.getDimensionPixelSize(attr, 20);
                    break;
                case R.styleable.LabelShowView_dateTextColor:
                    mDateTextColor = a.getColor(attr, Color.BLACK);
                    break;
                case R.styleable.LabelShowView_timeShowGap:
                    mTimeShowGap = a.getDimensionPixelSize(attr, 10);
                    break;
                default:
                    break;
            }
        }

        a.recycle();

        final Resources res = getResources();

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.density = res.getDisplayMetrics().density;

        // Set drawable bounds
        if (mTimelineDotDrawable != null) {
            mTimelineDotDrawable.setBounds(0, 0,
                    mTimelineDotDrawable.getMinimumWidth(),
                    mTimelineDotDrawable.getMinimumHeight());
        }

        if (mLeftLabelLeafDrawable != null) {
            mLeftLabelLeafDrawable.setBounds(0, 0,
                    mLeftLabelLeafDrawable.getMinimumWidth(),
                    mLeftLabelLeafDrawable.getMinimumHeight());
            mLeftLabelLeafDrawable.getPadding(mLeftLabelLeafDrawablePadding);
        }
        if (mLeftLabelBranchDrawable != null) {
            mLeftLabelBranchDrawable.setBounds(0, 0,
                    Math.max(mLeftLabelBranchWidth,
                            mLeftLabelBranchDrawable.getMinimumWidth()),
                    mLeftLabelBranchDrawable.getMinimumHeight());
        }

        if (mRightLabelLeafDrawable != null) {
            mRightLabelLeafDrawable.setBounds(0, 0,
                    mRightLabelLeafDrawable.getMinimumWidth(),
                    mRightLabelLeafDrawable.getMinimumHeight());
            mRightLabelLeafDrawable.getPadding(mRightLabelLeafDrawablePadding);
        }
        if (mRightLabelBranchDrawable != null) {
            mRightLabelBranchDrawable.setBounds(0, 0,
                    Math.max(mRightLabelBranchWidth,
                            mRightLabelBranchDrawable.getMinimumWidth()),
                    mRightLabelBranchDrawable.getMinimumHeight());
        }

        mMonthString = res.getString(R.string.user_label_show_month);
        mDateString = res.getString(R.string.user_label_show_date);
    }

    private static final class SetLabelParam {

        public final Point mDesiredSize;
        public final List<LabelItem> mItemList;

        public SetLabelParam(List<LabelItem> itemList, Point desiredSize) {
            mItemList = itemList;
            mDesiredSize = desiredSize;
        }
    }

    public synchronized void setListener(IListener listener) {
        if (listener == null) {
            mListener = null;
        } else {
            mListener = new WeakReference<IListener>(listener);
        }
    }

    private synchronized IListener getListener() {
        if (mListener != null) {
            return mListener.get();
        } else {
            return null;
        }
    }

    public void setUserLabels(UserLabel[] labels) {
        List<LabelItem> itemList = new ArrayList<LabelItem>();

        if (labels != null) {
            for (UserLabel label : labels) {
                itemList.add(buildLabelItem(label));
            }
            Collections.sort(itemList);
        }

        Point desiredSize = measureDesiredSize(itemList);

        post(new ObjectRunnable(new SetLabelParam(itemList, desiredSize)) {
            @Override
            public void run() {
                if (mObject instanceof SetLabelParam) {
                    SetLabelParam param = (SetLabelParam) mObject;

                    exitSelectMode();
                    mItemList = param.mItemList;
                    mDesiredWidth = param.mDesiredSize.x;
                    mDesiredHeight = param.mDesiredSize.y;
                    requestLayout();
                    invalidate();
                }
            }
        });
    }

    public boolean isInSelectMode() {
        return mMode == MODE_SELECT_MODE;
    }

    public UserLabel[] getSelectedUserLabels() {
        List<UserLabel> labelList = new ArrayList<UserLabel>();

        for (WeakReference<LabelItem> ref : mSelectItemList) {
            LabelItem item = ref.get();
            if (item != null) {
                labelList.add(item.mUserLabel);
            }
        }

        final int length = labelList.size();

        return (length > 0) ? labelList.toArray(new UserLabel[length]) : null;
    }

    private LabelItem buildLabelItem(UserLabel label) {
        return (new LabelItem(label));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mItemList == null || mItemList.size() <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int w = Math.max(mDesiredWidth, getSuggestedMinimumWidth())
                + getPaddingLeft() + getPaddingRight();
        int h = Math.max(mDesiredHeight, getSuggestedMinimumHeight())
                + getPaddingTop() + getPaddingBottom();
        int widthSize = resolveSizeAndState(w, widthMeasureSpec, 0);
        int heightSize = resolveSizeAndState(h, heightMeasureSpec, 0);
        setMeasuredDimension(widthSize, heightSize);
    }

    private Layout makeLayout(CharSequence text, TextPaint paint, float textSize) {
        paint.setTextSize(textSize);
        return new StaticLayout(text, paint,
                (int) Math.ceil(Layout.getDesiredWidth(text, paint)),
                Layout.Alignment.ALIGN_NORMAL, 1.f, 0, true);
    }

    private Point measureDesiredSize(List<LabelItem> itemList) {
        Point size = new Point(0, 0);

        if (itemList == null || itemList.size() <= 0) {
            return size;
        }

        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        final int paddingRight = getPaddingRight();
        final int paddingBottom = getPaddingBottom();

        int desiredWidth = 0;
        int desiredHeight = 0;
        int currSide = LABEL_SIDE_RIGHT;
        long prevItemTime = 0;
        int leafTextSize = 0;
        int branchWidth = 0;
        Rect leafPadding = mLeftLabelLeafDrawablePadding;
        int boundsWidth;
        int boundsHeight = 0;
        Calendar cdr = Calendar.getInstance();

        for (LabelItem item : itemList) {
            boolean sideChanged = (prevItemTime != item.mTime);
            int tmpLeafWidth;

            if (sideChanged) {
                currSide = (currSide == LABEL_SIDE_LEFT) ? LABEL_SIDE_RIGHT : LABEL_SIDE_LEFT;

                if (currSide == LABEL_SIDE_LEFT) {
                    leafTextSize = mLeftLabelLeafTextSize;
                    branchWidth = mLeftLabelBranchWidth;
                    leafPadding = mLeftLabelLeafDrawablePadding;
                } else {
                    leafTextSize = mRightLabelLeafTextSize;
                    branchWidth = mRightLabelBranchWidth;
                    leafPadding = mRightLabelLeafDrawablePadding;
                }
            }

            item.mNameLayout = makeLayout(item.mName, mTextPaint, leafTextSize);
            boundsWidth = item.mNameLayout.getWidth() + leafPadding.left + leafPadding.right;
            boundsHeight = item.mNameLayout.getHeight() + leafPadding.top + leafPadding.bottom;

            tmpLeafWidth = branchWidth + boundsWidth;

            desiredWidth = Math.max(desiredWidth, tmpLeafWidth);
            if (sideChanged) {
                desiredHeight += mTimelineDotDist * 3;
                int remainder = desiredHeight % mTimelineDotDist;
                desiredHeight += (remainder != 0) ? (mTimelineDotDist - remainder) : 0;

                // measure time text
                cdr.setTimeInMillis(item.mTime);
                String month = String.valueOf(cdr.get(Calendar.MONTH) + 1) + mMonthString;
                String date = String.valueOf(cdr.get(Calendar.DAY_OF_MONTH) + mDateString);
                item.mMonthLayout = makeLayout(month, mTextPaint, mMonthTextSize);
                item.mDateLayout = makeLayout(date, mTextPaint, mDateTextSize);
            } else {
                desiredHeight += mSameDateLabelDist + boundsHeight;
            }
            item.mSide = currSide;
            item.mIsSideHeader = sideChanged;
            item.mOffsetY = desiredHeight;
            prevItemTime = item.mTime;
        }

        desiredWidth *= 2;
        desiredWidth += paddingLeft + paddingRight;
        desiredHeight += boundsHeight / 2 + paddingTop + paddingBottom;
        size.set(desiredWidth, desiredHeight);

        return size;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mItemList == null || mItemList.size() <= 0) {
            return;
        }

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

        final int saveCount = canvas.getSaveCount();
        canvas.save();
        canvas.clipRect(paddingLeft, paddingTop, paddingLeft + hspace, paddingTop + vspace);

        int startX = paddingLeft + hspace / 2;
        int startY = paddingTop;

        // Draw time line dots
        if (mTimelineDotDrawable != null) {
            final int endY = startY + Math.max(vspace, mDesiredHeight);
            int x = startX - mTimelineDotDrawable.getBounds().width() / 2;
            int y = startY - mTimelineDotDrawable.getBounds().height() / 2;

            while (y < endY) {
                drawDrawable(mTimelineDotDrawable, canvas, x, y);
                y += mTimelineDotDist;
            }
        }

        // Draw labels
        for (LabelItem item : mItemList) {
            if (item.mSide == LABEL_SIDE_LEFT) {
                drawLeftLabel(item, canvas, startX, startY);
            } else {
                drawRightLabel(item, canvas, startX, startY);
            }
        }

        canvas.restoreToCount(saveCount);
    }

    private void drawDrawable(Drawable drawable, Canvas canvas, int x, int y) {
        canvas.save();
        canvas.translate(x, y);
        drawable.draw(canvas);
        canvas.restore();
    }

    private void drawText(Layout layout, Canvas canvas, int x, int y) {
        canvas.save();
        canvas.translate(x, y);
        layout.draw(canvas);
        canvas.restore();
    }

    private void drawLeftLabel(LabelItem item, Canvas canvas, int startX, int startY) {
        final int[] stateSet = (item.mState == LabelItem.STATE_SELECTED)
                ? SELECTED_STATE_SET : EMPTY_STATE_SET;
        int x = startX;
        int y = startY + item.mOffsetY;

        // draw branch drawable
        if (mLeftLabelBranchDrawable != null) {
            Rect bounds = mLeftLabelBranchDrawable.getBounds();
            int offsetX = bounds.width() - mLeftLabelBranchDrawable.getIntrinsicWidth() / 2;

            if (item.mIsSideHeader) {
                if (mLeftLabelBranchDrawable.isStateful()) {
                    mLeftLabelBranchDrawable.setState(stateSet);
                }
                drawDrawable(mLeftLabelBranchDrawable, canvas,
                        x - offsetX, y - bounds.height() / 2);
            }
            x -= offsetX;
        }

        // draw label leaf
        // draw background
        if (mLeftLabelLeafDrawable != null) {
            final int textWidth = item.mNameLayout.getWidth();
            final int textHeight = item.mNameLayout.getHeight();
            final int paddingLeft = mLeftLabelLeafDrawablePadding.left;
            final int paddingRight = mLeftLabelLeafDrawablePadding.right;
            final int paddingTop = mLeftLabelLeafDrawablePadding.top;
            final int paddingBottom = mLeftLabelLeafDrawablePadding.bottom;

            // set label leaf drawable bounds
            mLeftLabelLeafDrawable.setBounds(0, 0,
                    textWidth + paddingLeft + paddingRight,
                    textHeight + paddingTop + paddingBottom);
            Rect bounds = mLeftLabelLeafDrawable.getBounds();
            int drX = x - bounds.width();
            int drY = y - bounds.height() / 2;

            if (mLeftLabelLeafDrawable.isStateful()) {
                mLeftLabelLeafDrawable.setState(stateSet);
            }

            // Draw it
            drawDrawable(mLeftLabelLeafDrawable, canvas, drX, drY);

            mLeftLabelLeafDrawable.copyBounds(item.mShowBounds);
            item.mShowBounds.offset(drX, drY);
            x -= paddingRight;
        }

        // draw text
        mTextPaint.setTextSize(mLeftLabelLeafTextSize);
        mTextPaint.setColor(mLeftLabelLeafTextColor.getColorForState(stateSet, 0));
        drawText(item.mNameLayout, canvas, x - item.mNameLayout.getWidth(),
                y - item.mNameLayout.getHeight() / 2);

        // draw label time
        if (item.mIsSideHeader) {
            final int monthHeight = item.mMonthLayout.getHeight();
            final int dateHeight = item.mDateLayout.getHeight();
            final int dateWidth = item.mDateLayout.getWidth();

            x = startX + mTimeShowGap;
            y = startY + item.mOffsetY - dateHeight / 2;
            mTextPaint.setTextSize(mDateTextSize);
            mTextPaint.setColor(mDateTextColor);
            drawText(item.mDateLayout, canvas, x, y);

            x += dateWidth;
            y += dateHeight - monthHeight;
            mTextPaint.setTextSize(mMonthTextSize);
            mTextPaint.setColor(mMonthTextColor);
            drawText(item.mMonthLayout, canvas, x, y);
        }
    }

    private void drawRightLabel(LabelItem item, Canvas canvas, int startX, int startY) {
        final int[] stateSet = (item.mState == LabelItem.STATE_SELECTED)
                ? SELECTED_STATE_SET : EMPTY_STATE_SET;
        int x = startX;
        int y = startY + item.mOffsetY;

        // draw branch drawable
        if (mRightLabelBranchDrawable != null) {
            Rect bounds = mRightLabelBranchDrawable.getBounds();
            int offsetX = mLeftLabelBranchDrawable.getIntrinsicWidth() / 2;

            if (item.mIsSideHeader) {
                if (mRightLabelBranchDrawable.isStateful()) {
                    mRightLabelBranchDrawable.setState(stateSet);
                }
                drawDrawable(mRightLabelBranchDrawable, canvas,
                        x - offsetX, y - bounds.height() / 2);
            }
            x += bounds.width() - mLeftLabelBranchDrawable.getIntrinsicWidth() / 2;
        }

        // draw label leaf
        // draw background
        if (mRightLabelLeafDrawable != null) {
            final int textWidth = item.mNameLayout.getWidth();
            final int textHeight = item.mNameLayout.getHeight();
            final int paddingLeft = mRightLabelLeafDrawablePadding.left;
            final int paddingRight = mRightLabelLeafDrawablePadding.right;
            final int paddingTop = mRightLabelLeafDrawablePadding.top;
            final int paddingBottom = mRightLabelLeafDrawablePadding.bottom;

            // set label leaf drawable bounds
            mRightLabelLeafDrawable.setBounds(0, 0,
                    textWidth + paddingLeft + paddingRight,
                    textHeight + paddingTop + paddingBottom);
            Rect bounds = mRightLabelLeafDrawable.getBounds();
            int drX = x;
            int drY = y - bounds.height() / 2;

            if (mRightLabelLeafDrawable.isStateful()) {
                mRightLabelLeafDrawable.setState(stateSet);
            }

            // Draw it
            drawDrawable(mRightLabelLeafDrawable, canvas, drX, drY);

            mRightLabelLeafDrawable.copyBounds(item.mShowBounds);
            item.mShowBounds.offset(drX, drY);
            x += paddingLeft;
        }

        // draw text
        mTextPaint.setTextSize(mRightLabelLeafTextSize);
        mTextPaint.setColor(mRightLabelLeafTextColor.getColorForState(stateSet, 0));
        drawText(item.mNameLayout, canvas, x, y - item.mNameLayout.getHeight() / 2);

        // draw label time
        if (item.mIsSideHeader) {
            final int monthHeight = item.mMonthLayout.getHeight();
            final int monthWidth = item.mMonthLayout.getWidth();
            final int dateHeight = item.mDateLayout.getHeight();
            final int dateWidth = item.mDateLayout.getWidth();

            x = startX - monthWidth - dateWidth - mTimeShowGap;
            y = startY + item.mOffsetY - dateHeight / 2;
            mTextPaint.setTextSize(mDateTextSize);
            mTextPaint.setColor(mDateTextColor);
            drawText(item.mDateLayout, canvas, x, y);

            x += dateWidth;
            y += dateHeight - monthHeight;
            mTextPaint.setTextSize(mMonthTextSize);
            mTextPaint.setColor(mMonthTextColor);
            drawText(item.mMonthLayout, canvas, x, y);
        }
    }

    private LabelItem getLabelItemByPoint(float x, float y) {
        LabelItem item = null;

        if (mItemList != null) {
            for (LabelItem tmpItem : mItemList) {
                Rect bounds = tmpItem.mShowBounds;

                if (bounds.contains((int) x, (int) y)) {
                    item = tmpItem;
                    break;
                }
            }
        }

        return item;
    }

    private final class PerformItemClick implements Runnable {
        public void run() {
            if (mTouchSelectedItem != null) {
                if (isInSelectMode()) {
                    onItemSelected(mTouchSelectedItem);
                } else {
                    onItemClicked(mTouchSelectedItem);
                }
            }
        }
    }

    private final class PerformItemLongClick implements Runnable {
        public void run() {
            mHasPerformedItemLongPress = true;
            if (mTouchSelectedItem != null) {
                if (isInSelectMode()) {
                    exitSelectMode();
                } else {
                    entrySelectMode();
                    onItemSelected(mTouchSelectedItem);
                }
            }
        }
    }

    private void onItemClicked(LabelItem item) {
        IListener listener = getListener();
        if (listener != null) {
            listener.onLabelClicked(item.mUserLabel);
        }
    }

    private void onItemSelected(LabelItem item) {
        boolean isSelected = !item.getSelected();
        item.setSelected(isSelected);

        if (isSelected) {
            mSelectItemList.add(new WeakReference<LabelItem>(item));
        } else {
            for (int i = mSelectItemList.size() - 1; i >= 0; --i) {
                if (mSelectItemList.get(i).get() == item) {
                    mSelectItemList.remove(i);
                }
            }
        }

        IListener listener = getListener();
        if (listener != null) {
            listener.onLabelSelected(item.mUserLabel, isSelected);
        }
        invalidate();
    }

    public void exitSelectMode() {
        if (!isInSelectMode()) {
            return;
        }

        mMode = MODE_NORMAL_MODE;
        for (WeakReference<LabelItem> ref : mSelectItemList) {
            LabelItem item = ref.get();
            if (item != null) {
                item.setSelected(false);
            }
        }
        mSelectItemList.clear();

        IListener listener = getListener();
        if (listener != null) {
            listener.onExitSelectMode();
        }

        invalidate();
    }

    private void entrySelectMode() {
        if (isInSelectMode()) {
            return;
        }

        mMode = MODE_SELECT_MODE;
        for (WeakReference<LabelItem> ref : mSelectItemList) {
            LabelItem item = ref.get();
            if (item != null) {
                item.setSelected(false);
            }
        }
        mSelectItemList.clear();

        IListener listener = getListener();
        if (listener != null) {
            listener.onEntrySelectMode();
        }
    }

    private LabelItem mTouchSelectedItem;
    private boolean mHasPerformedItemLongPress = false;
    private final PerformItemClick mPerformItemClick = new PerformItemClick();
    private final PerformItemLongClick mPerformItemLongClick = new PerformItemLongClick();

    private void checkForItemLongClick(int delayOffset) {
        if (mTouchSelectedItem != null) {
            mHasPerformedItemLongPress = false;
            postDelayed(mPerformItemLongClick,
                    ViewConfiguration.getLongPressTimeout() - delayOffset);
        }
    }

    private void removeItemLongPressCallback() {
        removeCallbacks(mPerformItemLongClick);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();
        boolean handled = false;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mTouchSelectedItem = getLabelItemByPoint(x, y);
                if (mTouchSelectedItem != null) {
                    checkForItemLongClick(0);
                    handled = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchSelectedItem != null
                        && mTouchSelectedItem.mShowBounds.contains((int) x, (int) y)) {
                    handled = true;
                } else {
                    mTouchSelectedItem = null;
                    removeItemLongPressCallback();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTouchSelectedItem != null) {
                    if (!mHasPerformedItemLongPress) {
                        post(mPerformItemClick);
                    }
                    handled = true;
                }
                removeItemLongPressCallback();
                break;
            case MotionEvent.ACTION_CANCEL:
                removeItemLongPressCallback();
                break;
            default:
                break;
        }

        return handled || super.onTouchEvent(event);
    }
}

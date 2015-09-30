package com.ekuater.labelchat.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;

import com.ekuater.labelchat.R;

/**
 * Created by Administrator on 2015/2/8.
 * @author Fan Chong
 */
public class AddLineGridView extends GridView {
    public AddLineGridView(Context context) {
        super(context);
    }

    public AddLineGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AddLineGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
//        Paint paint = new Paint();
//        paint.setStyle(Paint.Style.FILL_AND_STROKE);
//        paint.setColor(getContext().getResources().getColor(R.color.divider_color));
//        int childCount = getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            View cellView = getChildAt(i);
//            canvas.drawLine(cellView.getLeft(), cellView.getBottom(), cellView.getRight() * 3, cellView.getBottom(), paint);
//        }

    }
}

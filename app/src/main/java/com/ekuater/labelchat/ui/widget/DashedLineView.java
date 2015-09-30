package com.ekuater.labelchat.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

import com.ekuater.labelchat.R;

/**
 * Created by Label on 2015/2/10.
 */
public class DashedLineView extends View {

    public DashedLineView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        paint.setColor(getResources().getColor(R.color.dasheline));
        Path path = new Path();
        path.moveTo(0, 10);
        path.lineTo(wm.getDefaultDisplay().getWidth(),10);
        PathEffect effects = new DashPathEffect(new float[]{5,5,5,5},1);
        paint.setPathEffect(effects);
        canvas.drawPath(path, paint);
    }
}

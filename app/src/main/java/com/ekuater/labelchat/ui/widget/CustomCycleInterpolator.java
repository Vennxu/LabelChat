package com.ekuater.labelchat.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.CycleInterpolator;

/**
 * @author LinYong
 */
public class CustomCycleInterpolator extends CycleInterpolator {

    public CustomCycleInterpolator(float cycles) {
        super(cycles);
    }

    public CustomCycleInterpolator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public float getInterpolation(float input) {
        return super.getInterpolation(input) * (1.0f - input);
    }
}

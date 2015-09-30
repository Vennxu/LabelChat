
package com.ekuater.labelchat.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class GroupInformationGridView extends GridView {
    public GroupInformationGridView(Context context) {
        super(context);
    }

    public GroupInformationGridView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public GroupInformationGridView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

    }
}

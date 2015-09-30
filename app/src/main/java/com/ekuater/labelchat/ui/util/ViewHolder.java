package com.ekuater.labelchat.ui.util;

import android.support.v4.util.SparseArrayCompat;
import android.view.View;

/**
 * Created by Label on 2015/1/8.
 *
 * @author Xu wenxiang
 */
public class ViewHolder {

    public static View get(View view, int id) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        if (viewHolder == null) {
            viewHolder = new ViewHolder();
            view.setTag(viewHolder);
        }

        View child = viewHolder.get(id);
        if (child == null) {
            child = view.findViewById(id);
            viewHolder.put(id, child);
        }

        return child;
    }

    private final SparseArrayCompat<View> mViews;

    public ViewHolder() {
        mViews = new SparseArrayCompat<View>();
    }

    public View get(int id) {
        return mViews.get(id);
    }

    public void put(int id, View view) {
        mViews.put(id, view);
    }
}

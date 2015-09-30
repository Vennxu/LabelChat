package com.ekuater.labelchat.ui.fragment.personalinfo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ekuater.labelchat.R;

/**
 * Created by Leo on 2015/4/1.
 *
 * @author LinYong
 */
public class GapViewItem implements ViewItem {

    public GapViewItem() {
    }

    @Override
    public int getViewType() {
        return ViewType.TYPE_GAP;
    }

    @Override
    public View newView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.personal_info_gap_item, parent, false);
    }

    @Override
    public void bindView(View view) {
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void onClick() {
    }
}

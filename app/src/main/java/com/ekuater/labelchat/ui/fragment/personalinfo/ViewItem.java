package com.ekuater.labelchat.ui.fragment.personalinfo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Leo on 2015/3/13.
 *
 * @author LinYong
 */
public interface ViewItem {

    public int getViewType();

    public View newView(LayoutInflater inflater, ViewGroup parent);

    public void bindView(View view);

    public boolean isEnabled();

    public void onClick();
}

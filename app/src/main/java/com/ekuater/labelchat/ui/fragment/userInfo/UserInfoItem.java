package com.ekuater.labelchat.ui.fragment.userInfo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2015/3/12.
 *
 * @author FanChong
 */
public interface UserInfoItem {

    View newView(LayoutInflater layoutInflater, ViewGroup parent);

    void bindView(View view);
}

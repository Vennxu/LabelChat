package com.ekuater.labelchat.ui.fragment.mixdynamic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ekuater.labelchat.datastruct.mixdynamic.DynamicWrapper;

/**
 * Created by Leo on 2015/4/16.
 *
 * @author LinYong
 */
public interface DynamicRender {

    public View getView(LayoutInflater inflater, ViewGroup parent);

    public void bindEvents();

    public void bindView(DynamicWrapper dynamicWrapper, int position);

    public void unbindView();
}

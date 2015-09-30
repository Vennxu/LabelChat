package com.ekuater.labelchat.ui.fragment.labelstory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ekuater.labelchat.datastruct.LabelStory;

/**
 * Created by Leo on 2015/4/20.
 *
 * @author LinYong
 */
public interface StoryContentRender {

    public void onCreate();

    public View onCreateView(LayoutInflater inflater, ViewGroup container);

    public void bindContentData(LabelStory story);

    public void onDestroyView();

    public void onDestroy();
}

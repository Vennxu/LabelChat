package com.ekuater.labelchat.command.labelstory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Label on 2015/3/16.
 */
public class CommentItem {

    public interface Item{
        public void newView(LayoutInflater inflater, ViewGroup parent);
        public void bindView(View view);
        public int getViewType();
        public void onClicke();
    }

}

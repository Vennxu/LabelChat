package com.ekuater.labelchat.ui.fragment.userInfo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.ekuater.labelchat.R;

/**
 * Created by Administrator on 2015/4/28.
 */
public class PersonalItem {

    public static interface UserClickListener{

        public void letterClick();

        public void tallClick();

        public void inviteClick();

        public void attentionClick();

        public void addClick();

        public void unFollowClick();

        public void reportClick();

        public void deleteFriendClick();

    }

    public static interface UserItem{

        public View newView(LayoutInflater layoutInflater, ViewGroup parent);

        public void bindView(View view);

    }
}

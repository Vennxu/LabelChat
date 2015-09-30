package com.ekuater.labelchat.ui.fragment.friends;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.ekuater.labelchat.R;

import com.ekuater.labelchat.datastruct.Stranger;

/**
 * Created by Administrator on 2015/1/28.
 *
 * @author Fan Chong
 */
public class RecommendFriendFragment extends Fragment {
    private Stranger mStranger;


    public static RecommendFriendFragment newInstance(Stranger stranger) {
        RecommendFriendFragment instance = new RecommendFriendFragment();
        instance.mStranger = stranger;
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_show, container, false);
        return view;
    }


}

package com.ekuater.labelchat.ui.fragment.mixdynamic;

import android.view.View;

import com.ekuater.labelchat.datastruct.Confide;
import com.ekuater.labelchat.datastruct.LabelStory;

/**
 * Created by Leo on 2015/4/17.
 *
 * @author LinYong
 */
public interface ConfideDynamicListener {

    public void onConfideItemClick(Confide confide, boolean isShowSoft, int position);

    public boolean onConfideItemLongClick(Confide confide, int position);

    public void onConfidePraise(Confide confide, int position);

    public void onConfideComment(int position);

    public void onConfideChildComment(String replyName, int position, int childPosition);

    public void onConfideCommentTxClick(String userId);
	
	 public void onConfideMoerClick(Confide confide, int position, View v);
}

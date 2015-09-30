package com.ekuater.labelchat.ui.fragment.mixdynamic;

import android.view.View;

import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.LabelStoryComments;
import com.ekuater.labelchat.datastruct.mixdynamic.DynamicWrapper;

/**
 * Created by Leo on 2015/4/17.
 *
 * @author LinYong
 */
public interface StoryDynamicListener {

    void onStoryClick(LabelStory story, int position);

    boolean onStoryLongClick(LabelStory story, int position);

    void onStoryPraiseClick(LabelStory story, int position);

    void onStoryLetterClick(LabelStory story, int position);

    void onStoryAvatarClick(LabelStory story, int position);

    void onStoryFollowingClick(LabelStory story, int position);

    void onStoryMoreClick(LabelStory story, int position, View v);

    void onStoryComment(int position, LabelStory story);

    void onStoryChildComment(String replyName, int position, int childPosition);

    void onStoryCommentTxClick(String userId);

    void onStoryImageClick(String imageUrl);

    void onStoryMoreImageClick(String imageUrl[], int position);
}

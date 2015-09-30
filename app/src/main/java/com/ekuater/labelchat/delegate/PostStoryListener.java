package com.ekuater.labelchat.delegate;

import com.ekuater.labelchat.datastruct.LabelStory;

/**
 * Created by Label on 2015/1/15.
 *
 * @author Xu wenxiang
 */
public interface PostStoryListener {

    void onPostResult(int result, int errorCode, String errorDesc,
                      LabelStory[] labelStories);
}

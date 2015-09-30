package com.ekuater.labelchat.ui.fragment.labelstory;

import android.content.Context;

import com.ekuater.labelchat.datastruct.LabelStory;

/**
 * Created by Leo on 2015/4/21.
 *
 * @author LinYong
 */
public class StoryRenderFactory {

    public static StoryContentRender newRender(Context context, LabelStory story) {
        StoryContentRender render;

        switch (story.getType()) {
            case LabelStory.TYPE_ONLINEAUDIO:
            case LabelStory.TYPE_AUDIO:
                render = new AudioStoryRender(context);
                break;
            case LabelStory.TYPE_BANKNOTE:
                render = new BanknoteStoryRender(context);
                break;
            case LabelStory.TYPE_TXT_IMG:
            default:
                render = new TxtStoryRender(context);
                break;
        }
        return render;
    }
}

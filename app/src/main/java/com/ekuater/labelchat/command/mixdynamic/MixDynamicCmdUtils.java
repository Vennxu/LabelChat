package com.ekuater.labelchat.command.mixdynamic;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.confide.ConfideCmdUtils;
import com.ekuater.labelchat.command.labelstory.LabelStoryCmdUtils;
import com.ekuater.labelchat.datastruct.Confide;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.mixdynamic.DynamicWrapper;
import com.ekuater.labelchat.datastruct.mixdynamic.WrapperUtils;
import com.ekuater.labelchat.util.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Leo on 2015/4/16.
 *
 * @author LinYong
 */
public final class MixDynamicCmdUtils {

    private static final String TAG = MixDynamicCmdUtils.class.getSimpleName();

    public static DynamicWrapper toWrapper(JSONObject json) {
        if (json == null) {
            return null;
        }

        DynamicWrapper wrapper = null;

        try {
            String contentType = json.getString(CommandFields.Dynamic.CONTENT_TYPE);

            switch (contentType) {
                case CommandFields.Dynamic.CONTENT_TYPE_STORY:
                    wrapper = toStoryWrapper(json);
                    break;
                case CommandFields.Dynamic.CONTENT_TYPE_CONFIDE:
                    wrapper = toConfideWrapper(json);
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            L.w(TAG, e);
        }

        return wrapper;
    }

    private static void mappingKey(JSONObject json, String[][] keyMap) throws JSONException {
        for (String[] item : keyMap) {
            if (json.has(item[0])) {
                json.put(item[1], json.remove(item[0]));
            }
        }
    }

    private static final String[][] STORY_KEY_MAP = new String[][]{
            {CommandFields.Dynamic.OBJECT_ID, CommandFields.StoryLabel.LABEL_STORY_ID},
            {CommandFields.User.USER_ID, CommandFields.StoryLabel.AUTHOR_USER_ID},
            {CommandFields.Confide.CONFIDE_PRAISE_NUM, CommandFields.StoryLabel.PRAISE},
            {CommandFields.Dynamic.DYNAMIC_COMMENT_ARRAY, CommandFields.StoryLabel.STORY_COMMENT_ARRAY}
    };

    private static DynamicWrapper toStoryWrapper(JSONObject json)
            throws JSONException {
        mappingKey(json, STORY_KEY_MAP);

        LabelStory story = LabelStoryCmdUtils.toLabelStory(json);
        if (story == null) {
            return null;
        }

        try {
            return WrapperUtils.fromStory(story);
        } catch (Exception e) {
            L.w(TAG, e);
            return null;
        }
    }

    private static final String[][] CONFIDE_KEY_MAP = new String[][]{
            {CommandFields.Dynamic.OBJECT_ID, CommandFields.Confide.CONFIDE_ID},
            {CommandFields.Dynamic.DYNAMIC_COMMENT_ARRAY, CommandFields.Confide.CONFIDE_COMMENT_ARRAY}
    };

    private static DynamicWrapper toConfideWrapper(JSONObject json)
            throws JSONException {
        mappingKey(json, CONFIDE_KEY_MAP);

        Confide confide = ConfideCmdUtils.toConfide(json);
        if (confide != null) {
            return WrapperUtils.fromConfide(confide);
        } else {
            return null;
        }
    }

    public static DynamicWrapper[] toWrapperArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }

        ArrayList<DynamicWrapper> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                DynamicWrapper wrapper = toWrapper(json);
                if (wrapper != null) {
                    list.add(wrapper);
                }
            }
        }

        final int size = list.size();
        return (size > 0) ? list.toArray(new DynamicWrapper[size]) : null;
    }
}

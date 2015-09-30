package com.ekuater.labelchat.datastruct;

import com.ekuater.labelchat.command.contact.ContactCmdUtils;
import com.ekuater.labelchat.command.labelstory.LabelStoryCmdUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2015/5/15.
 *
 * @author Xu wenxiang
 */
public class DynamicRemaindMessage {

    private long time;
    private LabelStory labelStory;
    private LiteStranger stranger;

    public DynamicRemaindMessage(LiteStranger stranger, LabelStory story) {
        this.stranger = stranger;
        this.labelStory = story;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public LabelStory getLabelStory() {
        return labelStory;
    }

    public LiteStranger getStranger() {
        return stranger;
    }

    public static DynamicRemaindMessage build(JSONObject json) throws JSONException {
        DynamicRemaindMessage newMessage = null;
        if (json != null) {
            LiteStranger stranger = ContactCmdUtils.toLiteStranger(
                    json.optJSONObject(SystemPushFields.FIELD_CONFIDE_USER_VO));
            LabelStory story = LabelStoryCmdUtils.toLabelStory(
                    json.optJSONObject(SystemPushFields.FIELD_DYNAMIC_INFO_VO));

            if (stranger != null && story != null) {
                newMessage = new DynamicRemaindMessage(stranger, story);
            }
        }
        return newMessage;
    }

    public static DynamicRemaindMessage build(SystemPush systemPush) {
        DynamicRemaindMessage newMessage = null;
        try {
            newMessage = DynamicRemaindMessage.build(new JSONObject(systemPush.getContent()));
            if (newMessage != null) {
                newMessage.setTime(systemPush.getTime());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newMessage;
    }
}

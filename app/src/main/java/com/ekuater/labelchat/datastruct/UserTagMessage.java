package com.ekuater.labelchat.datastruct;


import com.ekuater.labelchat.command.contact.ContactCmdUtils;
import com.ekuater.labelchat.command.tag.TagCmdUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2015/5/15.
 *
 * @author Xu wenxiang
 */
public class UserTagMessage {


    private long time;
    private UserTag userTag;
    private LiteStranger stranger;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public UserTag getUserTag() {
        return userTag;
    }

    public void setUserTag(UserTag userTag) {
        this.userTag = userTag;
    }

    public LiteStranger getStranger() {
        return stranger;
    }

    public void setStranger(LiteStranger stranger) {
        this.stranger = stranger;
    }

    public static UserTagMessage build(JSONObject json) throws JSONException {
        UserTagMessage newMessage = null;
        if (json != null) {
            newMessage = new UserTagMessage();
            newMessage.setStranger(ContactCmdUtils.toLiteStranger(json.optJSONObject(SystemPushFields.FIELD_CONFIDE_USER_VO)));
            newMessage.setUserTag(TagCmdUtils.toUserTag(json.optJSONObject(SystemPushFields.FILED_TAG_VO)));
        }
        return newMessage;
    }

    public static UserTagMessage build(SystemPush systemPush) {
        UserTagMessage newMessage = null;
        try {
            newMessage = UserTagMessage.build(new JSONObject(systemPush.getContent()));
            newMessage.setTime(systemPush.getTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newMessage;
    }
}

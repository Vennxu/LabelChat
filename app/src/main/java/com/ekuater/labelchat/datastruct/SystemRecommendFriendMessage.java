package com.ekuater.labelchat.datastruct;

import com.ekuater.labelchat.command.contact.ContactCmdUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Leo on 2015/1/27.
 *
 * @author LinYong
 */
public class SystemRecommendFriendMessage {

    private Stranger[] mRecommendFriends;

    public SystemRecommendFriendMessage() {
    }

    public Stranger[] getRecommendFriends() {
        return mRecommendFriends;
    }

    public void setRecommendFriends(Stranger[] strangers) {
        mRecommendFriends = strangers;
    }

    public static SystemRecommendFriendMessage build(JSONObject json) {
        final Stranger[] strangers = ContactCmdUtils.toStrangerArray(
                json.optJSONArray(SystemPushFields.FIELD_STRANGERS));
        SystemRecommendFriendMessage newMessage = null;

        if (strangers != null && strangers.length > 0) {
            newMessage = new SystemRecommendFriendMessage();
            newMessage.setRecommendFriends(strangers);
        }

        return newMessage;
    }

    public static SystemRecommendFriendMessage build(SystemPush push) {
        SystemRecommendFriendMessage newMessage = null;

        if (push.getType() == SystemPushType.TYPE_SYSTEM_RECOMMEND_FRIEND) {
            try {
                newMessage = build(new JSONObject(push.getContent()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return newMessage;
    }
}

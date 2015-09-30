package com.ekuater.labelchat.datastruct;

import com.ekuater.labelchat.command.following.FollowCmdUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Leo on 2015/3/25.
 *
 * @author LinYong
 */
public class BeenFollowedMessage {

    public static final int TYPE_FOLLOWED = 0;
    public static final int TYPE_CANCEL_FOLLOW = 1;

    private String message;
    private FollowUser followUser;
    private int followType;
    private long time;

    public BeenFollowedMessage() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public FollowUser getFollowUser() {
        return followUser;
    }

    public void setFollowUser(FollowUser followUser) {
        this.followUser = followUser;
    }

    public int getFollowType() {
        return followType;
    }

    public void setFollowType(int followType) {
        this.followType = followType;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public static BeenFollowedMessage build(JSONObject json) {
        BeenFollowedMessage newMessage = null;

        if (json != null) {
            String message = json.optString(SystemPushFields.FIELD_MESSAGE);
            FollowUser user = FollowCmdUtils.toFollowUser(
                    json.optJSONObject(SystemPushFields.FIELD_STRANGER));
            int followType = json.optInt(SystemPushFields.FIELD_FOLLOW_TYPE);

            if (user != null) {
                newMessage = new BeenFollowedMessage();
                newMessage.setMessage(message);
                newMessage.setFollowUser(user);
                newMessage.setFollowType(followType);
            }
        }

        return newMessage;
    }

    public static BeenFollowedMessage build(SystemPush push) {
        BeenFollowedMessage newMessage = null;

        if (push.getType() == SystemPushType.TYPE_BEEN_FOLLOWED) {
            try {
                newMessage = build(new JSONObject(push.getContent()));
                if (newMessage != null) {
                    newMessage.setTime(push.getTime());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return newMessage;
    }
}

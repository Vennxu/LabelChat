package com.ekuater.labelchat.datastruct;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
public class LoginInOtherClientMessage {

    private static long mTime;

    public LoginInOtherClientMessage() {
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public static LoginInOtherClientMessage build(JSONObject json) {
        LoginInOtherClientMessage newMessage = null;

        try {
            final long time = json.getLong(SystemPushFields.FIELD_TIME);

            newMessage = new LoginInOtherClientMessage();
            newMessage.setTime(time);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return newMessage;
    }

    public static LoginInOtherClientMessage build(SystemPush push) {
        LoginInOtherClientMessage newMessage = null;

        if (push.getType() == SystemPushType.TYPE_LOGIN_ON_OTHER_CLIENT) {
            try {
                newMessage = build(new JSONObject(push.getContent()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return newMessage;
    }
}

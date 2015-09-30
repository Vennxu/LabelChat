package com.ekuater.labelchat.datastruct;

import com.ekuater.labelchat.command.contact.ContactCmdUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Leo on 2015/3/25.
 *
 * @author LinYong
 */
public class BeenInvitedMessage {

    private LiteStranger stranger;
    private long time;

    public BeenInvitedMessage() {

    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public LiteStranger getStranger() {
        return stranger;
    }

    public void setStranger(LiteStranger stranger) {
        this.stranger = stranger;
    }

    public static BeenInvitedMessage build(JSONObject json) {
        BeenInvitedMessage newMessage = null;

        if (json != null) {
            LiteStranger stranger = ContactCmdUtils.toLiteStranger(
                    json.optJSONObject(SystemPushFields.FIELD_STRANGER));

            if (stranger != null) {
                newMessage = new BeenInvitedMessage();
                newMessage.setStranger(stranger);
            }
        }

        return newMessage;
    }

    public static BeenInvitedMessage build(SystemPush push) {
        BeenInvitedMessage newMessage = null;

        if (push.getType() == SystemPushType.TYPE_BEEN_INVITED) {
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

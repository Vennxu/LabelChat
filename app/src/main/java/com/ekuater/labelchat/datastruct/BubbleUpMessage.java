package com.ekuater.labelchat.datastruct;

import com.ekuater.labelchat.command.contact.ContactCmdUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
public class BubbleUpMessage {

    private Stranger[] mStrangers;

    public BubbleUpMessage() {
    }

    public Stranger[] getBubbleUpStrangers() {
        return mStrangers;
    }

    public void setBubbleUpStrangers(Stranger[] strangers) {
        mStrangers = strangers;
    }

    public static BubbleUpMessage build(JSONObject json) {
        final Stranger[] strangers = ContactCmdUtils.toStrangerArray(
                json.optJSONArray(SystemPushFields.FIELD_STRANGERS));
        BubbleUpMessage newMessage = null;

        if (strangers != null && strangers.length > 0) {
            newMessage = new BubbleUpMessage();
            newMessage.setBubbleUpStrangers(strangers);
        }

        return newMessage;
    }

    public static BubbleUpMessage build(SystemPush push) {
        BubbleUpMessage newMessage = null;

        if (push.getType() == SystemPushType.TYPE_BUBBLE_UP) {
            try {
                newMessage = build(new JSONObject(push.getContent()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return newMessage;
    }
}

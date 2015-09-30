package com.ekuater.labelchat.datastruct;

import com.ekuater.labelchat.command.contact.ContactCmdUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
public class TodayRecommendedMessage {

    private Stranger[] mRecommendedStrangers;

    public TodayRecommendedMessage() {
    }

    public Stranger[] getRecommendedStrangers() {
        return mRecommendedStrangers;
    }

    public void setRecommendedStrangers(Stranger[] strangers) {
        mRecommendedStrangers = strangers;
    }

    public static TodayRecommendedMessage build(JSONObject json) {
        final Stranger[] strangers = ContactCmdUtils.toStrangerArray(
                json.optJSONArray(SystemPushFields.FIELD_STRANGERS));
        TodayRecommendedMessage newMessage = null;

        if (strangers != null && strangers.length > 0) {
            newMessage = new TodayRecommendedMessage();
            newMessage.setRecommendedStrangers(strangers);
        }

        return newMessage;
    }

    public static TodayRecommendedMessage build(SystemPush push) {
        TodayRecommendedMessage newMessage = null;

        if (push.getType() == SystemPushType.TYPE_TODAY_RECOMMENDED) {
            try {
                newMessage = build(new JSONObject(push.getContent()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return newMessage;
    }
}

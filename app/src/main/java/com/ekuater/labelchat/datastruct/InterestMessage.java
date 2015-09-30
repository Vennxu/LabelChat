package com.ekuater.labelchat.datastruct;


import com.ekuater.labelchat.command.confide.ConfideCmdUtils;
import com.ekuater.labelchat.command.contact.ContactCmdUtils;
import com.ekuater.labelchat.command.interest.InterestCmdUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2015/5/15.
 *
 * @author Xu wenxiang
 */
public class InterestMessage {


    private long time;
    private UserInterest userInterest;
    private LiteStranger stranger;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public UserInterest getUserInterest() {
        return userInterest;
    }

    public void setUserInterest(UserInterest userInterest) {
        this.userInterest = userInterest;
    }

    public LiteStranger getStranger() {
        return stranger;
    }

    public void setStranger(LiteStranger stranger) {
        this.stranger = stranger;
    }

    public static InterestMessage build(JSONObject json) throws JSONException {
        InterestMessage newMessage = null;
        if (json != null) {
            newMessage = new InterestMessage();
            newMessage.setStranger(ContactCmdUtils.toLiteStranger(json.optJSONObject(SystemPushFields.FIELD_CONFIDE_USER_VO)));
            newMessage.setUserInterest(InterestCmdUtils.toUserInterest(json.optJSONObject(SystemPushFields.FILED_INTEREST_VO)));
        }
        return newMessage;
    }

    public static InterestMessage build(SystemPush systemPush) {
        InterestMessage newMessage = null;
        try {
            newMessage = InterestMessage.build(new JSONObject(systemPush.getContent()));
            newMessage.setTime(systemPush.getTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newMessage;
    }
}

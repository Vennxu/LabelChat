package com.ekuater.labelchat.datastruct;

import com.ekuater.labelchat.command.contact.ContactCmdUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
public class AddFriendRejectResultMessage {

    private String mMessage;
    private Stranger mStranger;

    public AddFriendRejectResultMessage() {
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setStranger(Stranger stranger) {
        mStranger = stranger;
    }

    public Stranger getStranger() {
        return mStranger;
    }

    public static AddFriendRejectResultMessage build(JSONObject json) {
        AddFriendRejectResultMessage newMessage = null;

        if (json != null) {
            try {
                Stranger stranger = ContactCmdUtils.toStranger(
                        json.getJSONObject(SystemPushFields.FIELD_STRANGER));
                String message = json.optString(SystemPushFields.FIELD_MESSAGE);

                if (stranger != null) {
                    newMessage = new AddFriendRejectResultMessage();
                    newMessage.setStranger(stranger);
                    newMessage.setMessage(message);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return newMessage;
    }

    public static AddFriendRejectResultMessage build(SystemPush push) {
        AddFriendRejectResultMessage newMessage = null;

        if (push.getType() == SystemPushType.TYPE_ADD_FRIEND_REJECT_RESULT) {
            try {
                newMessage = build(new JSONObject(push.getContent()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return newMessage;
    }
}

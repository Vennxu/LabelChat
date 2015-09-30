package com.ekuater.labelchat.datastruct;

import com.ekuater.labelchat.command.contact.ContactCmdUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
public class ValidateAddFriendMessage {

    public static final int STATE_AGREED = 100;
    public static final int STATE_REJECTED = 101;

    private Stranger mStranger;
    private String mValidateMessage;

    public ValidateAddFriendMessage() {
    }

    public Stranger getStranger() {
        return mStranger;
    }

    public void setStranger(Stranger stranger) {
        mStranger = stranger;
    }

    public String getValidateMessage() {
        return mValidateMessage;
    }

    public void setValidateMessage(String message) {
        mValidateMessage = message;
    }

    public static ValidateAddFriendMessage build(JSONObject json) {
        ValidateAddFriendMessage newMessage = null;

        if (json != null) {
            Stranger stranger = ContactCmdUtils.toStranger(
                    json.optJSONObject(SystemPushFields.FIELD_STRANGER));
            String message = json.optString(SystemPushFields.FIELD_MESSAGE);
            if (stranger != null) {
                newMessage = new ValidateAddFriendMessage();
                newMessage.setStranger(stranger);
                newMessage.setValidateMessage(message);
            }
        }

        return newMessage;
    }

    public static ValidateAddFriendMessage build(SystemPush push) {
        ValidateAddFriendMessage newMessage = null;

        if (push.getType() == SystemPushType.TYPE_VALIDATE_ADD_FRIEND) {
            try {
                newMessage = build(new JSONObject(push.getContent()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return newMessage;
    }
}

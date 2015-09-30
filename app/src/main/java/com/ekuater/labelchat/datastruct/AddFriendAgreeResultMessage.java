package com.ekuater.labelchat.datastruct;

import com.ekuater.labelchat.command.contact.ContactCmdUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
public class AddFriendAgreeResultMessage {

    private String mMessage;
    private UserContact mContact;

    public AddFriendAgreeResultMessage() {
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setContact(UserContact contact) {
        mContact = contact;
    }

    public UserContact getContact() {
        return mContact;
    }

    public static AddFriendAgreeResultMessage build(JSONObject json) {
        AddFriendAgreeResultMessage newMessage = null;

        if (json != null) {
            try {
                UserContact contact = ContactCmdUtils.toContact(
                        json.getJSONObject(SystemPushFields.FIELD_CONTACT));
                String message = json.optString(SystemPushFields.FIELD_MESSAGE);

                if (contact != null) {
                    newMessage = new AddFriendAgreeResultMessage();
                    newMessage.setContact(contact);
                    newMessage.setMessage(message);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return newMessage;
    }

    public static AddFriendAgreeResultMessage build(SystemPush push) {
        AddFriendAgreeResultMessage newMessage = null;

        if (push.getType() == SystemPushType.TYPE_ADD_FRIEND_AGREE_RESULT) {
            try {
                newMessage = build(new JSONObject(push.getContent()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return newMessage;
    }
}

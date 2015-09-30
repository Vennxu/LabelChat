package com.ekuater.labelchat.datastruct;

import com.ekuater.labelchat.command.contact.ContactCmdUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
public class FriendInfoUpdateMessage {

    private UserContact mContact;

    public FriendInfoUpdateMessage() {
    }

    public UserContact getContact() {
        return mContact;
    }

    public void setContact(UserContact contact) {
        mContact = contact;
    }

    public static FriendInfoUpdateMessage build(JSONObject json) {
        FriendInfoUpdateMessage newMessage = null;

        if (json != null) {
            UserContact contact = ContactCmdUtils.toContact(
                    json.optJSONObject(SystemPushFields.FIELD_CONTACT));
            if (contact != null) {
                newMessage = new FriendInfoUpdateMessage();
                newMessage.setContact(contact);
            }
        }

        return newMessage;
    }

    public static FriendInfoUpdateMessage build(SystemPush push) {
        FriendInfoUpdateMessage newMessage = null;

        if (push.getType() == SystemPushType.TYPE_FRIEND_INFO_UPDATED) {
            try {
                newMessage = build(new JSONObject(push.getContent()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return newMessage;
    }
}

package com.ekuater.labelchat.datastruct;

import com.ekuater.labelchat.command.contact.ContactCmdUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
public class AddFriendMessage {

    private UserContact mContact;

    public AddFriendMessage() {
    }

    public UserContact getContact() {
        return mContact;
    }

    public void setContact(UserContact contact) {
        mContact = contact;
    }

    public static AddFriendMessage build(JSONObject json) {
        AddFriendMessage newMessage = null;

        if (json != null) {
            UserContact contact = ContactCmdUtils.toContact(
                    json.optJSONObject(SystemPushFields.FIELD_CONTACT));
            if (contact != null) {
                newMessage = new AddFriendMessage();
                newMessage.setContact(contact);
            }
        }

        return newMessage;
    }

    public static AddFriendMessage build(SystemPush push) {
        AddFriendMessage newMessage = null;

        if (push.getType() == SystemPushType.TYPE_ADD_FRIEND) {
            try {
                newMessage = build(new JSONObject(push.getContent()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return newMessage;
    }
}

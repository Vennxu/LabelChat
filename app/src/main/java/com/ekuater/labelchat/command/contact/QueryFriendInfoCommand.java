package com.ekuater.labelchat.command.contact;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.UserContact;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class QueryFriendInfoCommand extends UserCommand {

    private static final String URL = CommandUrl.CONTACT_QUERY_FRIEND_INFO;

    public QueryFriendInfoCommand() {
        super();
        setUrl(URL);
    }

    public QueryFriendInfoCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamFriendUserId(String userId) {
        putParam(CommandFields.Normal.FRIEND_USER_ID, userId);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public UserContact getContact() {
            return ContactCmdUtils.toContact(getValueJson(CommandFields.Contact.CONTACT));
        }
    }
}
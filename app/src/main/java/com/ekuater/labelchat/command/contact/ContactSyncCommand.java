package com.ekuater.labelchat.command.contact;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.UserContact;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class ContactSyncCommand extends UserCommand {

    private static final String URL = CommandUrl.CONTACT_SYNC;

    public ContactSyncCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public UserContact[] getContacts() {
            return ContactCmdUtils.toContactArray(
                    getValueJsonArray(CommandFields.Contact.CONTACTS));
        }
    }
}

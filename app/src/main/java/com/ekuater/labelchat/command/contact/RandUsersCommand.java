package com.ekuater.labelchat.command.contact;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.Stranger;

import org.json.JSONException;

/**
 * Created by Leo on 2014/12/25.
 * @author LinYong
 */
public class RandUsersCommand extends UserCommand {

    private static final String URL = CommandUrl.CONTACT_RAND_USERS;

    public RandUsersCommand() {
        super();
        setUrl(URL);
    }

    public RandUsersCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamRandCount(int randCount) {
        putParam(CommandFields.Normal.RAND_COUNT, String.valueOf(randCount));
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public Stranger[] getRandUsers() {
            return ContactCmdUtils.toStrangerArray(
                    getValueJsonArray(CommandFields.Stranger.STRANGERS));
        }
    }
}

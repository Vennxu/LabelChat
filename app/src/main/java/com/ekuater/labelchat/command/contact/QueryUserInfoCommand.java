package com.ekuater.labelchat.command.contact;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.Stranger;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class QueryUserInfoCommand extends UserCommand {

    private static final String URL = CommandUrl.CONTACT_QUERY_USER_INFO;

    public QueryUserInfoCommand() {
        super();
        setUrl(URL);
    }

    public QueryUserInfoCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamStrangerUserId(String userId) {
        putParam(CommandFields.Normal.STRANGER_USER_ID, userId);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public Stranger getUserInfo() {
            return ContactCmdUtils.toStranger(getValueJson(CommandFields.Stranger.STRANGER));
        }
    }
}

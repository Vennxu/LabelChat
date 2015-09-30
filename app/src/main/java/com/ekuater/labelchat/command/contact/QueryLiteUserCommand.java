package com.ekuater.labelchat.command.contact;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.LiteStranger;

import org.json.JSONException;

/**
 * Created by Leo on 2015/3/3.
 *
 * @author LinYong
 */
public class QueryLiteUserCommand extends UserCommand {

    private static final String URL = CommandUrl.CONTACT_QUERY_LITE_USER;

    public QueryLiteUserCommand() {
        super();
        setUrl(URL);
    }

    public QueryLiteUserCommand(String session, String userId) {
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

        public LiteStranger getLiteStranger() {
            return ContactCmdUtils.toLiteStranger(getValueJson(
                    CommandFields.Stranger.STRANGER));
        }
    }
}

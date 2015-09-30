package com.ekuater.labelchat.command.confide;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;

import org.json.JSONException;

/**
 * Created by Leo on 2015/4/11.
 *
 * @author LinYong
 */
public class DeleteCommand extends UserCommand {

    private static final String URL = CommandUrl.CONFIDE_DELETE;

    public DeleteCommand() {
        super();
        setUrl(URL);
    }

    public DeleteCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamConfideId(String confideId) {
        putParam(CommandFields.Confide.CONFIDE_ID, confideId);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}

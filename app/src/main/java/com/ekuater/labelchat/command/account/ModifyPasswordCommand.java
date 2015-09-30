package com.ekuater.labelchat.command.account;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class ModifyPasswordCommand extends UserCommand {

    private static final String URL = CommandUrl.ACCOUNT_MODIFY_PASSWORD;

    public ModifyPasswordCommand() {
        super();
        setUrl(URL);
    }

    public ModifyPasswordCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamOldPassword(String password) {
        putParam(CommandFields.Normal.OLD_PASSWORD, password);
    }

    public void putParamNewPassword(String password) {
        putParam(CommandFields.Normal.NEW_PASSWORD, password);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}

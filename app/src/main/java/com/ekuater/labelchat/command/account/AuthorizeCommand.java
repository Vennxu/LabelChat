package com.ekuater.labelchat.command.account;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.SessionCommand;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
public class AuthorizeCommand extends SessionCommand {

    private static final String URL = CommandUrl.ACCOUNT_AUTHORIZE;

    public AuthorizeCommand() {
        super();
        setUrl(URL);
    }

    public AuthorizeCommand(String session) {
        this();
        setSession(session);
    }

    public static class CommandResponse extends SessionCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public String getEncryption() {
            return getValueString(CommandFields.Normal.ENCRYPTION);
        }

        public String getCertificate() {
            return getValueString(CommandFields.Normal.CERTIFICATE);
        }

        public JSONObject getUserInfo() {
            return getValueJson(CommandFields.User.USER);
        }
    }
}

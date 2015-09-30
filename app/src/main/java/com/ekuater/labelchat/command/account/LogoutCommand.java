
package com.ekuater.labelchat.command.account;

import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.SessionCommand;

import org.json.JSONException;

public class LogoutCommand extends SessionCommand {

    private static final String URL = CommandUrl.ACCOUNT_LOGOUT;

    public LogoutCommand() {
        super();
        setUrl(URL);
    }

    public LogoutCommand(String session) {
        this();
        setSession(session);
    }

    public static class CommandResponse extends SessionCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}


package com.ekuater.labelchat.command;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
public abstract class SessionCommand extends BaseCommand {

    private String mSession;

    public SessionCommand() {
        super();
    }

    public SessionCommand(String session) {
        this();
        setSession(session);
    }

    public void setSession(String session) {
        mSession = session;
        putParam(CommandFields.Base.SESSION, mSession);
    }

    @Override
    protected void putBaseParameters() {
        super.putBaseParameters();
        putParam(CommandFields.Base.SESSION, mSession);
    }

    public static class CommandResponse extends BaseCommand.CommandResponse {

        public CommandResponse(JSONObject response) {
            super(response);
        }

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public boolean isSessionInvalid() {
            return !executedSuccess() && getErrorCode()
                    == CommandErrorCode.SESSION_ID_INVALID;
        }
    }
}

package com.ekuater.labelchat.command;

import org.json.JSONException;

/**
 * @author LinYong
 */
public abstract class UserCommand extends SessionCommand {

    private String mUserId;

    public UserCommand() {
        super();
    }

    public UserCommand(String session, String userId) {
        super(session);
        setUserId(userId);
    }

    public void setUserId(String userId) {
        mUserId = userId;
        putParam(CommandFields.User.USER_ID, userId);
    }

    @Override
    protected void putBaseParameters() {
        super.putBaseParameters();
        putParam(CommandFields.User.USER_ID, mUserId);
    }

    public static class CommandResponse extends SessionCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}

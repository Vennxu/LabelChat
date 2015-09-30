package com.ekuater.labelchat.command.contact;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class WeeklyStarConfirmCommand extends UserCommand {

    private static final String URL = CommandUrl.CONTACT_WEEKLY_STAR_CONFIRM;

    public WeeklyStarConfirmCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void setParamSession(String session) {
        putParam(CommandFields.Normal.WEEKLY_STAR_SESSION, session);
    }

    public void setParamAccept(boolean accept) {
        putParam(CommandFields.Normal.ACCEPT, accept);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}

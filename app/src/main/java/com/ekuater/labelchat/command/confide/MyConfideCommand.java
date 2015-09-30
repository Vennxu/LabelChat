package com.ekuater.labelchat.command.confide;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.Confide;

import org.json.JSONException;

/**
 * Created by Leo on 2015/4/11.
 *
 * @author LinYong
 */
public class MyConfideCommand extends UserCommand {

    private static final String URL = CommandUrl.CONFIDE_MY_CONFIDE;

    public MyConfideCommand() {
        super();
        setUrl(URL);
    }

    public MyConfideCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamRequestTime(String requestTime) {
        putParam(CommandFields.Normal.REQUEST_TIME, requestTime);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public Confide[] getConfides() {
            return ConfideCmdUtils.toConfideArray(getValueJsonArray(
                    CommandFields.Confide.CONFIDE_ARRAY));
        }
    }
}

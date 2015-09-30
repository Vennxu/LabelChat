package com.ekuater.labelchat.command.account;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.LocationInfo;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class UpdatePositionCommand extends UserCommand {

    private static final String URL = CommandUrl.ACCOUNT_UPDATE_LOCATION;

    public UpdatePositionCommand() {
        super();
        setUrl(URL);
    }

    public UpdatePositionCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamLocation(LocationInfo location) {
        putParam(CommandFields.User.LONGITUDE, String.valueOf(location.getLongitude()));
        putParam(CommandFields.User.LATITUDE, String.valueOf(location.getLatitude()));
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}

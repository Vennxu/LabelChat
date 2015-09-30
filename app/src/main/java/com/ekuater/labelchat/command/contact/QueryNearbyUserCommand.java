package com.ekuater.labelchat.command.contact;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.Stranger;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class QueryNearbyUserCommand extends UserCommand {

    private static final String URL = CommandUrl.CONTACT_QUERY_NEARBY_USER;

    public QueryNearbyUserCommand() {
        super();
        setUrl(URL);
    }

    public QueryNearbyUserCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamPosition(LocationInfo location) {
        putParam(CommandFields.User.LONGITUDE, String.valueOf(location.getLongitude()));
        putParam(CommandFields.User.LATITUDE, String.valueOf(location.getLatitude()));
    }

    public void putParamDistance(int distance) {
        putParam(CommandFields.User.DISTANCE, String.valueOf(distance));
    }

    public void putParamRequestTime(int requestTime) {
        putParam(CommandFields.Normal.REQUEST_TIME, String.valueOf(requestTime));
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public Stranger[] getQueryUsers() {
            return ContactCmdUtils.toStrangerArray(getValueJsonArray(CommandFields.Stranger.STRANGERS));
        }
    }
}

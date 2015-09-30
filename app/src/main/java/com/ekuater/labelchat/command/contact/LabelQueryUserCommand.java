package com.ekuater.labelchat.command.contact;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.command.labels.LabelCmdUtils;
import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.Stranger;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * @author LinYong
 */
public class LabelQueryUserCommand extends UserCommand {

    private static final String URL = CommandUrl.CONTACT_LABEL_QUERY_FRIEND;

    public LabelQueryUserCommand(String session, String userId, String labelCode) {
        super(session, userId);
        setUrl(URL);
        putParamLabelCode(labelCode);
    }

    private void putParamLabelCode(String labelCode) {
        putParam(CommandFields.User.LABEL_CODE, labelCode);
    }

    public void putParamLabels(BaseLabel[] labels) {
        JSONArray jsonArray = LabelCmdUtils.toJsonArray(labels);

        if (jsonArray != null) {
            putParam(CommandFields.BaseLabel.LABELS, jsonArray);
        }
    }

    public void putParamRequestTime(int requestTime) {
        putParam(CommandFields.Normal.REQUEST_TIME, String.valueOf(requestTime));
    }

    public void putParamLocation(LocationInfo location) {
        putParam(CommandFields.User.LONGITUDE, String.valueOf(location.getLongitude()));
        putParam(CommandFields.User.LATITUDE, String.valueOf(location.getLatitude()));
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public Stranger[] getQueryUsers() {
            return ContactCmdUtils.toStrangerArray(getValueJsonArray(CommandFields.Stranger.STRANGERS));
        }

        public boolean isRemaining() {
            return getValueBoolean(CommandFields.Normal.REMAINING);
        }
    }
}

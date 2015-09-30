package com.ekuater.labelchat.command.contact;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.PraiseStranger;
import com.ekuater.labelchat.datastruct.Stranger;

import org.json.JSONException;

/**
 * Created by Administrator on 2015/1/17.
 *
 * @author FanChong
 */
public class OneLabelQueryUserCommand extends UserCommand {

    private static final String URL = CommandUrl.CONTACT_QUERY_FRIEND_BY_ONE_LABEL;

    public OneLabelQueryUserCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamLabelId(String labelId) {
        putParam(CommandFields.UserLabel.LABEL_ID, labelId);
    }

    public void putParamRequestTime(int requestTime) {
        putParam(CommandFields.Normal.REQUEST_TIME, String.valueOf(requestTime));
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public boolean isRemaining() {
            return getValueBoolean(CommandFields.Normal.REMAINING);
        }

        public PraiseStranger[] getPraiseStrangers() {
            return ContactCmdUtils.toPraiseStrangerArray(getValueJsonArray(
                    CommandFields.Stranger.STRANGERS));
        }
    }
}

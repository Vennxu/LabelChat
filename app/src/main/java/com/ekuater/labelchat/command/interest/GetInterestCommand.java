package com.ekuater.labelchat.command.interest;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.InterestType;

import org.json.JSONException;

/**
 * Created by Administrator on 2015/3/21.
 *
 * @author FanChong
 */
public class GetInterestCommand extends UserCommand {
    private static final String URL = CommandUrl.INTEREST_GET_INTEREST;

    public GetInterestCommand() {
        super();
        setUrl(URL);
    }

    public GetInterestCommand(String sessionId, String userId) {
        super(sessionId, userId);
        setUrl(URL);
    }

    public void putParamQueryUserId(String queryUserId) {
        putParam(CommandFields.Normal.QUERY_USER_ID, queryUserId);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public InterestType[] getInterests() {
            return InterestCmdUtils.toInterestTypeArray(getValueJsonArray(CommandFields.Interest.INTEREST_TYPE_ARRAY));
        }
    }
}
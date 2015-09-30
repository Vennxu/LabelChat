package com.ekuater.labelchat.command.interest;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.UserInterest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Administrator on 2015/3/21.
 *
 * @author FanChong
 */
public class SetInterestCommand extends UserCommand {
    private static final String URL = CommandUrl.INTEREST_SET_INTEREST;

    public SetInterestCommand() {
        super();
        setUrl(URL);
    }

    public SetInterestCommand(String sessionId, String userId) {
        super(sessionId, userId);
        setUrl(URL);
    }

    public void putParamInterestTypeId(int typeId){
        putParam(CommandFields.Interest.INTEREST_TYPE_ID, typeId);
    }

    public void putParamInterestName(String interestName){
        putParam(CommandFields.Interest.ADD_INTEREST, interestName);
    }

    public void putParamInterestNameArray(UserInterest[] userInterests) {
        JSONArray jsonArray = new JSONArray();
        for (UserInterest userInterest : userInterests) {
            jsonArray.put(userInterest.getInterestName());
        }
        putParam(CommandFields.Interest.INTEREST_ARRAY, jsonArray);
    }

    public void putParamArrayUserId(List<String> arrayUserId) {
        if (arrayUserId != null && arrayUserId.size() > 0) {
            JSONArray jsonArray = new JSONArray();
            for (String userId : arrayUserId) {
                jsonArray.put(userId);
            }
            putParam(CommandFields.StoryLabel.RELATED_USER_IDS, jsonArray);
        }
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException, JSONException {
            super(response);
        }
    }
}

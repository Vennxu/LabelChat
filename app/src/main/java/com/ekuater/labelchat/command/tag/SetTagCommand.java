package com.ekuater.labelchat.command.tag;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Leo on 2015/3/15.
 *
 * @author LinYong
 */
public class SetTagCommand extends UserCommand {

    private static final String URL = CommandUrl.TAG_SET_TAG;

    public SetTagCommand() {
        super();
        setUrl(URL);
    }

    public SetTagCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamAddTagId(String addTagId){
        putParam(CommandFields.Tag.ADD_TAG_ID, addTagId);
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

    public void putParamTagIdArray(int[] tagIds) {
        JSONArray jsonArray = new JSONArray();

        for (int tagId : tagIds) {
            jsonArray.put(tagId);
        }
        putParam(CommandFields.Tag.TAG_ID_ARRAY, jsonArray);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}

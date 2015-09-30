package com.ekuater.labelchat.command.labelstory;

import com.ekuater.labelchat.command.CommandErrorCode;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.LabelStory;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Label on 2015/1/5.
 */
public class PraiseLabelStoryCommand extends UserCommand {

    public static final String URL = CommandUrl.LABEL_STORY_PRAISE;
    public static final String COMENTS_URL = CommandUrl.LABEL_STORY_COMMENTS_PRAISE;

    public PraiseLabelStoryCommand() {
        super();
        setUrl(URL);
    }

    public PraiseLabelStoryCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }
    public PraiseLabelStoryCommand(String session,String userId,String url){
        super(session, userId);
        setUrl(url);
    }
    public void putParamLabelStoryId(String labelStoryId) {
        putParam(CommandFields.StoryLabel.LABEL_STORY_ID, labelStoryId);
    }
    public void putParamLabelStoryCommentId(String labelStoryCommentId){
        putParam(CommandFields.StoryLabel.STORY_COMMENT_ID,labelStoryCommentId);
    }

    public void putParamArrayUserId(List<String> arrayUserId) {

        JSONArray jsonArray = new JSONArray();
        if (arrayUserId != null && arrayUserId.size() > 0) {
            for (String userId : arrayUserId) {
                jsonArray.put(userId);
            }
        }
        putParam(CommandFields.StoryLabel.RELATED_USER_IDS, jsonArray);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public boolean requestExit() {
            return getErrorCode() == CommandErrorCode.DATA_ALREADY_EXIST;
        }
    }
}

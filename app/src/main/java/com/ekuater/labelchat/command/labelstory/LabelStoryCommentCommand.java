package com.ekuater.labelchat.command.labelstory;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.LabelStoryComments;
import com.ekuater.labelchat.ui.fragment.labelstory.LabelStoryUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Label on 2014/12/31
 *
 * @author Xu wenxiang.
 */
public class LabelStoryCommentCommand extends UserCommand {

    private static final String URL = CommandUrl.LABEL_STORY_COMMAND;

    public LabelStoryCommentCommand(){
        super();
        setUrl(URL);
    }
    public LabelStoryCommentCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamLabelStoryId(String labelStoryId){
        putParam(CommandFields.StoryLabel.LABEL_STORY_ID,labelStoryId);
    }

    public void putParamCommentContent(String commentContent){
        putParam(CommandFields.StoryLabel.STORY_COMMENT,commentContent+" ");
    }
    public void putParamParentCommentId(String parentCommentId) {
        putParam(CommandFields.StoryLabel.PARENT_COMMENT_ID, parentCommentId);
    }

    public void putParamReplyNickName(String replayNickName) {
        putParam(CommandFields.StoryLabel.REPLY_NICK_NAME, replayNickName);
    }
    public void putParamReplyUserId(String replyUserId){
        putParam(CommandFields.StoryLabel.REPLY_USER_ID,replyUserId);
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

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
        public LabelStoryComments getLabelStoryComment() throws JSONException {
            return LabelStoryCmdUtils.toLabelStoryComments(getValueJson(CommandFields.StoryLabel.LABEL_STORY_COMMENT_VO));
        }
    }
}

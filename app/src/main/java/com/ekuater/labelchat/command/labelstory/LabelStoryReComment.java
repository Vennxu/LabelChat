package com.ekuater.labelchat.command.labelstory;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.LabelStoryChildComment;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Label on 2015/2/6.
 */
public class LabelStoryReComment extends UserCommand {


    private static final String URL = CommandUrl.LABEL_STORY_REPLY_COMMENTS;

    public LabelStoryReComment() {
        super();
        setUrl(URL);
    }

    public LabelStoryReComment(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamLabelStoryId(String labelStoryId) {
        putParam(CommandFields.StoryLabel.LABEL_STORY_ID, labelStoryId);
    }

    public void putParamStoryComment(String storyComment) {
        putParam(CommandFields.StoryLabel.STORY_COMMENT, storyComment);
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
    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public LabelStoryChildComment toChildComment() throws JSONException {
            return LabelStoryCmdUtils.toLabelStoryChildComments(getValueJson(CommandFields.StoryLabel.LABEL_STORY_COMMENT_VO));
        }

    }



}

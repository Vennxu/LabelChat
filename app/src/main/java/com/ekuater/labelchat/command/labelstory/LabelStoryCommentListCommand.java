package com.ekuater.labelchat.command.labelstory;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.SessionCommand;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.LabelStoryComments;

import org.json.JSONException;

/**
 * Created by Label on 2014/12/31
 *
 * @author Xu wenxiang.
 */
public class LabelStoryCommentListCommand extends UserCommand {

    private static final String URL = CommandUrl.LABEL_STORY_COMMANDLIST;

    public LabelStoryCommentListCommand(){
        super();
        setUrl(URL);
    }
    public LabelStoryCommentListCommand(String session,String userId) {
        super(session,userId);
        setUrl(URL);
    }

    public void putParamLabelStoryId(String labelStoryId){
        putParam(CommandFields.StoryLabel.LABEL_STORY_ID,labelStoryId);
    }

    public void putParamRequestTime(String requestTime){
        putParam(CommandFields.StoryLabel.REQUEST_TIME,requestTime);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public LabelStory getLabelStory() {
            return LabelStoryCmdUtils.toLabelStory(getValueJson(CommandFields.StoryLabel.LABEL_STORY_VO));
        }
    }

}

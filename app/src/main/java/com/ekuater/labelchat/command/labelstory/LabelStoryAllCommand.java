package com.ekuater.labelchat.command.labelstory;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.LabelStory;

import org.json.JSONException;

/**
 * Created by Label on 2015/1/14.
 */
public class LabelStoryAllCommand extends UserCommand{
    private static final String URL = CommandUrl.LABEL_STORY_LATESTSTORY;

    public LabelStoryAllCommand(){
        super();
        setUrl(URL);
    }
    public LabelStoryAllCommand(String session, String userId ,String url) {
        super(session, userId);
        setUrl(url);
    }


    public void putParamRequestTime(String requestTime){
        putParam(CommandFields.StoryLabel.REQUEST_TIME,requestTime);
    }
    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public LabelStory[] getLabelStory() {
            return LabelStoryCmdUtils.toLabelStoryArray(getValueJsonArray(CommandFields.StoryLabel.LABEL_STORY_ARRAY));
        }
    }
}

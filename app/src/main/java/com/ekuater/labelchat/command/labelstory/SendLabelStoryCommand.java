package com.ekuater.labelchat.command.labelstory;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.LabelStory;

import org.json.JSONException;

/**
 * Created by Label on 2015/1/5.
 */
public class SendLabelStoryCommand extends UserCommand {

    public static final String URL= CommandUrl.LABEL_STORY_POST;
    public static final String IMAGEURL= CommandUrl.LABEL_STORY_IMAGEPOST;

    public SendLabelStoryCommand(){
        super();
        setUrl(URL);
    }

    public SendLabelStoryCommand(String session,String userId){
        super(session,userId);
        setUrl(URL);
    }

    public void putParamLabelId(String categoryId){

        putParam(CommandFields.StoryLabel.CATEGORY_ID,categoryId);

    }
    public void putParamLabelStoryContent(String labelStoryContent){
        putParam(CommandFields.StoryLabel.STORY_CONTENT,labelStoryContent);
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

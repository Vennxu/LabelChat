package com.ekuater.labelchat.command.labelstory;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.LabelStoryCategory;

import org.json.JSONException;

/**
 * Created by Label on 2015/3/11.
 */
public class LabelStoryCategoryCommand extends UserCommand {

    private static final String URL = CommandUrl.LABEL_STORY_CATEGORY;

    public LabelStoryCategoryCommand(){
        super();
        setUrl(URL);
    }

    public static class CommandResponse extends UserCommand.CommandResponse{

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public LabelStoryCategory[] getCatetory() throws JSONException {
            return LabelStoryCmdUtils.toCatetoryArray(getValueJsonArray(CommandFields.StoryLabel.DYNAMIC_CATEGORY_ARRAY));
        }
    }

}

package com.ekuater.labelchat.command.labelstory;

import com.ekuater.labelchat.command.CommandErrorCode;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;

import org.json.JSONException;

/**
 * Created by wenxiang on 2015/3/2.
 */
public class LabelStoryDeleteCommand extends UserCommand{

    private static final String URL= CommandUrl.LABEL_STORY_DELETE;

    public LabelStoryDeleteCommand(){
        super();
        setUrl(URL);
    }

    public LabelStoryDeleteCommand(String session,String userId){
        super(session,userId);
        setUrl(URL);
    }

    public void putParamLabelStoryId(String labelStoryId){
        putParam(CommandFields.StoryLabel.LABEL_STORY_ID,labelStoryId);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {
        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}

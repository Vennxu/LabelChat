package com.ekuater.labelchat.command.labelstory;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;

import org.json.JSONException;

/**
 * Created by Label on 2015/3/13.
 */
public class SendLetterCommand extends UserCommand{

    private static final String URL = CommandUrl.LABEL_STORY_LETTER_SEND;

    public SendLetterCommand (){
        super();
        setUrl(URL);
    }

    public SendLetterCommand (String session, String userId){
        super(session,userId);
        setUrl(URL);
    }

    public void putParamLabelStoryId(String labelStoryId){
        putParam(CommandFields.StoryLabel.LABEL_STORY_ID, labelStoryId);
    }

    public void putParamStrangerUserId(String strangerUserId){
        putParam(CommandFields.Stranger.STRANGER_USER_ID, strangerUserId);
    }

    public void putParamMessage(String message){
        putParam(CommandFields.StoryLabel.MESSAGE, message);
    }

    public static class CommandResponse extends UserCommand.CommandResponse{

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}

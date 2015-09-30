package com.ekuater.labelchat.command.confide;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.Confide;
import com.ekuater.labelchat.datastruct.ConfideComment;

import org.json.JSONException;

/**
 * Created by Administrator on 2015/4/8.
 */
public class ConfideCommentCommand extends UserCommand{

    private static final String URL = CommandUrl.CONFIDE_COMMENT_LIST;

    public ConfideCommentCommand(){
        super();
        setUrl(URL);
    }

    public ConfideCommentCommand(String session, String userId){
        super(session, userId);
        setUrl(URL);
    }

    public void putParamRequstTime(String requestTime){
        putParam(CommandFields.StoryLabel.REQUEST_TIME, requestTime);
    }

    public void putParamConfideId(String confideId){
        putParam(CommandFields.Confide.CONFIDE_ID, confideId);
    }

    public static class CommandResponse extends BaseCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public Confide getConfide(){
            return ConfideCmdUtils.toConfide(getValueJson(CommandFields.Confide.CONFIDE_VO));
        }

    }
}

package com.ekuater.labelchat.command.confide;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.Confide;
import com.ekuater.labelchat.datastruct.ConfideComment;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Administrator on 2015/4/9.
 */
public class CommentCommand extends UserCommand{

    private static final String URL = CommandUrl.CONFIDE_COMMENT;

    public CommentCommand (){
        super();
        setUrl(URL);
    }

    public CommentCommand (String session, String userId){
        super(session, userId);
        setUrl(URL);
    }

    public void putParamConfideId(String confideId){
        putParam(CommandFields.Confide.CONFIDE_ID, confideId);
    }

    public void putParamComment(String comment){
        putParam(CommandFields.Confide.CONFIDE_COMMENT, comment);
    }

    public void putParamPosition(String position){
        putParam(CommandFields.Confide.CONFIDE_POSITION, position);
    }

    public void putParamParentCommentId(String parentCommentId){
        putParam(CommandFields.Confide.CONFIDE_COMMENT_PARENT, parentCommentId);
    }

    public void putParamReplyFloor(String replyFloor){
        putParam(CommandFields.Confide.CONFIDE_REPLY_FLOOR, replyFloor);
    }

    public void putParamReplyComment(String replyComment){
        putParam(CommandFields.Confide.CONFIDE_REPLY_COMMENT, replyComment);
    }

    public void putParamArrayUserId(List<String> arrayUserId) {
        if (arrayUserId != null && arrayUserId.size() > 0) {
            JSONArray jsonArray = new JSONArray();
            for (String userId : arrayUserId) {
                jsonArray.put(userId);
            }
            putParam(CommandFields.Confide.CONFIDE_COMMENT_RELATED_USER_IDS, jsonArray);
        }
    }


    public static class CommandResponse extends BaseCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public ConfideComment getConfideComent(){
            return ConfideCmdUtils.toConfideComment(getValueJson(CommandFields.Confide.CONFIDE_COMMENT_VO));
        }

    }


}

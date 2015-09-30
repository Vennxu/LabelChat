package com.ekuater.labelchat.command.recent;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.command.contact.ContactCmdUtils;
import com.ekuater.labelchat.command.interest.InterestCmdUtils;
import com.ekuater.labelchat.datastruct.RecentVisitor;
import com.ekuater.labelchat.datastruct.Stranger;

import org.json.JSONException;

/**
 * Created by Administrator on 2015/3/26.
 */

public class RecentCommand extends UserCommand{

    private static final String URL = CommandUrl.RECENT_VISITOR;

    public RecentCommand(){
        super();
        setUrl(URL);
    }

    public RecentCommand(String session,String userId){
        super(session,userId);
        setUrl(URL);
    }

    public void putParamQueryUserId(String queryUserId){
        putParam(CommandFields.StoryLabel.QUERY_USER_ID, queryUserId);
    }

    public static class CommandResponse extends UserCommand.CommandResponse{

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public RecentVisitor[] getRecentVisitor(){
            return InterestCmdUtils.toRecentVisitorArray(getValueJsonArray(CommandFields.User.USERS));
        }
    }
}

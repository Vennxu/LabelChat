package com.ekuater.labelchat.command.following;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;

import org.json.JSONException;

/**
 * Created by Label on 2015/3/12.
 */
public class FollowerCountCommand extends UserCommand {
    private static final String URL = CommandUrl.COUNT_FOLLOWER;

    public FollowerCountCommand(){
        super();
        setUrl(URL);
    }

    public FollowerCountCommand(String session, String userId){
        super(session,userId);
        setUrl(URL);
    }

    public static class CommandResponse extends UserCommand.CommandResponse{

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public int followerCount(){
            return getValueInt(CommandFields.Following.FOLLOWER_COUNT);
        }
    }
}

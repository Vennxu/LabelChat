package com.ekuater.labelchat.command.following;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.FollowUser;

import org.json.JSONException;

/**
 * Created by Label on 2015/3/12.
 */
public class FollowingCommand extends UserCommand {

    private static final String URL = CommandUrl.FOLLOWING;

    public FollowingCommand() {
        super();
        setUrl(URL);
    }

    public FollowingCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamFollowId(String followUserId) {
        putParam(CommandFields.Following.FOLLOW_USER_ID, followUserId);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public FollowUser getFollowingUser() {
            return FollowCmdUtils.toFollowUser(getValueJson(CommandFields.Stranger.STRANGER));
        }

        public int getFollowerCount() {
            return getValueInt(CommandFields.Following.FOLLOWER_COUNT);
        }
    }
}

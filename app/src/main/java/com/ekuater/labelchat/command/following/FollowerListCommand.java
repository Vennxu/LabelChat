package com.ekuater.labelchat.command.following;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.FollowUser;

import org.json.JSONException;

/**
 * Created by Label on 2015/3/12.
 *
 * @author LinYong
 */
public class FollowerListCommand extends UserCommand {

    private static final String URL = CommandUrl.LIST_FOLLOWER;

    public FollowerListCommand() {
        super();
        setUrl(URL);
    }

    public FollowerListCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public FollowUser[] getFollowerUsers() {
            return FollowCmdUtils.toFollowUserArray(getValueJsonArray(
                    CommandFields.Stranger.STRANGERS));
        }
    }
}

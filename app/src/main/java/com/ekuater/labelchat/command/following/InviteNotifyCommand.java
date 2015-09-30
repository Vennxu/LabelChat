package com.ekuater.labelchat.command.following;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;

import org.json.JSONException;

/**
 * Created by Administrator on 2015/3/25.
 * @author FanChong
 */
public class InviteNotifyCommand extends UserCommand {
    private static final String URL = CommandUrl.SEND_INVITE_NOTIFY;

    public InviteNotifyCommand() {
        super();
        setUrl(URL);
    }

    public InviteNotifyCommand(String sessionId, String userId) {
        super(sessionId, userId);
        setUrl(URL);
    }

    public void putParamUserId(String userId) {
        putParam(CommandFields.Following.INVITE_USER_ID, userId);
    }
    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }

}

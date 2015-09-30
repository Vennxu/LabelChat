package com.ekuater.labelchat.command.contact;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class DeleteFriendCommand extends UserCommand {

    private static final String URL = CommandUrl.CONTACT_DELETE_FRIEND;

    public DeleteFriendCommand(String session, String userId, String labelCode) {
        super(session, userId);
        setUrl(URL);
        putParamLabelCode(labelCode);
    }

    private void putParamLabelCode(String labelCode) {
        putParam(CommandFields.User.LABEL_CODE, labelCode);
    }

    public void putParamFriendUserId(String userId) {
        putParam(CommandFields.Normal.FRIEND_USER_ID, userId);
    }

    public void putParamFriendLabelCode(String labelCode) {
        putParam(CommandFields.Normal.FRIEND_LABEL_CODE, labelCode);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}

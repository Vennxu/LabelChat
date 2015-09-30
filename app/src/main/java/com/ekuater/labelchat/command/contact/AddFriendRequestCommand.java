package com.ekuater.labelchat.command.contact;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class AddFriendRequestCommand extends UserCommand {

    private static final String URL = CommandUrl.CONTACT_ADD_FRIEND_REQUEST;

    public AddFriendRequestCommand(String session, String userId, String labelCode) {
        super(session, userId);
        setUrl(URL);
        putParamLabelCode(labelCode);
    }

    private void putParamLabelCode(String labelCode) {
        putParam(CommandFields.User.LABEL_CODE, labelCode);
    }

    public void putParamRequestUserId(String userId) {
        putParam(CommandFields.Normal.REQUEST_USER_ID, userId);
    }

    public void putParamRequestLabelCode(String labelCode) {
        putParam(CommandFields.Normal.REQUEST_LABEL_CODE, labelCode);
    }

    public void putParamRequestNickname(String nickname) {
        putParam(CommandFields.Normal.REQUEST_NICK_NAME, nickname);
    }

    public void putParamVerifyMsg(String verifyMsg) {
        putParam(CommandFields.Normal.VERIFY_MSG, verifyMsg);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}

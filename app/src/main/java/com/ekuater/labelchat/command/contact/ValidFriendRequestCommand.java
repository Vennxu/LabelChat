package com.ekuater.labelchat.command.contact;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class ValidFriendRequestCommand extends UserCommand {

    private static final String URL = CommandUrl.CONTACT_VALID_FRIEND_REQUEST;

    public ValidFriendRequestCommand(String session, String userId, String labelCode) {
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

    public void putParamAccept(boolean accept) {
        putParam(CommandFields.Normal.VERIFY_TYPE, accept
                ? CommandFields.Normal.VERIFY_TYPE_AGREE
                : CommandFields.Normal.VERIFY_TYPE_REJECT);
    }

    public void putParamFriendRemark(String friendRemark) {
        putParam(CommandFields.Contact.REMARK, friendRemark);
    }

    public void putParamRejectMsg(String msg) {
        putParam(CommandFields.Normal.REJECT_MSG, msg);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}

package com.ekuater.labelchat.command.contact;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class ModifyFriendRemarkCommand extends UserCommand {

    private static final String URL = CommandUrl.CONTACT_MODIFY_FRIEND_REMARK;

    public ModifyFriendRemarkCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamFriendUserId(String userId) {
        putParam(CommandFields.Normal.FRIEND_USER_ID, userId);
    }

    public void putParamFriendRemark(String remark) {
        putParam(CommandFields.Normal.FRIEND_REMARK, remark);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}

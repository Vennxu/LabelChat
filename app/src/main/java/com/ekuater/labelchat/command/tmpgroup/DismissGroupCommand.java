package com.ekuater.labelchat.command.tmpgroup;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class DismissGroupCommand extends UserCommand {

    private static final String URL = CommandUrl.TMP_GROUP_DISMISS;

    public DismissGroupCommand() {
        super();
        setUrl(URL);
    }

    public DismissGroupCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamGroupId(String groupId) {
        putParam(CommandFields.TmpGroup.GROUP_ID, groupId);
    }

    public void putParamReason(String reason) {
        putParam(CommandFields.TmpGroup.REASON, reason);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}

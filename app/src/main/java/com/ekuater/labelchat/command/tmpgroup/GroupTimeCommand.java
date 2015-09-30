package com.ekuater.labelchat.command.tmpgroup;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.TmpGroupTime;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class GroupTimeCommand extends UserCommand {

    private static final String URL = CommandUrl.TMP_GROUP_GROUP_TIME;

    public GroupTimeCommand() {
        super();
        setUrl(URL);
    }

    public GroupTimeCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamGroupId(String groupId) {
        putParam(CommandFields.TmpGroup.GROUP_ID, groupId);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public TmpGroupTime getGroupTime() {
            return TmpGroupCmdUtils.toTmpGroupTime(getValueJson(CommandFields.TmpGroup.GROUP));
        }
    }
}

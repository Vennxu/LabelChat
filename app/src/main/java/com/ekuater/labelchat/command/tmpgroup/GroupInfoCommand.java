package com.ekuater.labelchat.command.tmpgroup;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.TmpGroup;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class GroupInfoCommand extends UserCommand {

    private static final String URL = CommandUrl.TMP_GROUP_GROUP_INFO;

    public GroupInfoCommand() {
        super();
        setUrl(URL);
    }

    public GroupInfoCommand(String session, String userId) {
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

        public TmpGroup getGroup() {
            return TmpGroupCmdUtils.toTmpGroup(getValueJson(CommandFields.TmpGroup.GROUP));
        }
    }
}

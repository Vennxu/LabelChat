package com.ekuater.labelchat.command.tmpgroup;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class QuitGroupCommand extends UserCommand {

    private static final String URL = CommandUrl.TMP_GROUP_MEMBER_QUIT;

    public QuitGroupCommand() {
        super();
        setUrl(URL);
    }

    public QuitGroupCommand(String session, String userId) {
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
    }
}

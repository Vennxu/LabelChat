package com.ekuater.labelchat.command.account;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.SystemPush;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
public class PullSystemPushCommand extends UserCommand {

    private static final String URL = CommandUrl.ACCOUNT_PULL_SYSTEM_PUSH;

    public PullSystemPushCommand() {
        super();
        setUrl(URL);
    }

    public void putParamPushId(String pushId) {
        putParam(CommandFields.Normal.PUSH_ID, pushId);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public SystemPush getSystemPush() {
            JSONObject content = getValueJson(CommandFields.Normal.CONTENT);

            if (content != null) {
                return SystemPush.build(content);
            } else {
                return null;
            }
        }
    }
}

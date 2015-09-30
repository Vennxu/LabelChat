package com.ekuater.labelchat.command.account;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class UpdateRongCloudTokenCommand extends UserCommand {

    private static final String URL = CommandUrl.ACCOUNT_RONGCLOUD_GET_TOKEN;

    public UpdateRongCloudTokenCommand() {
        super();
        setUrl(URL);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public String getToken() {
            return getValueString(CommandFields.Normal.TOKEN);
        }
    }
}

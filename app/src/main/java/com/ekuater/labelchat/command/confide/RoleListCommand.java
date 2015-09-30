package com.ekuater.labelchat.command.confide;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.datastruct.ConfideRole;

import org.json.JSONException;

/**
 * Created by Leo on 2015/4/7.
 *
 * @author LinYong
 */
public class RoleListCommand extends BaseCommand {

    private static final String URL = CommandUrl.CONFIDE_ROLE_LIST;

    public RoleListCommand() {
        super();
        setUrl(URL);
    }

    public static class CommandResponse extends BaseCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public ConfideRole[] getRoleArray() {
            return ConfideCmdUtils.toRoleArray(getValueJsonArray(
                    CommandFields.Confide.ROLE_ARRAY));
        }
    }
}

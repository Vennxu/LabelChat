package com.ekuater.labelchat.command.account;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class QueryPersonalInfoCommand extends UserCommand {

    private static final String TAG = QueryPersonalInfoCommand.class.getSimpleName();

    private static final String URL = CommandUrl.ACCOUNT_QUERY_PERSONAL_INFO;

    public QueryPersonalInfoCommand() {
        super();
        setUrl(URL);
    }

    public QueryPersonalInfoCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public PersonalInfo getPersonalInfo() {
            PersonalInfo info = null;

            try {
                info = new PersonalInfo(getValueJson(CommandFields.User.USER));
            } catch (Exception e) {
                L.w(TAG, e);
            }

            return info;
        }
    }
}

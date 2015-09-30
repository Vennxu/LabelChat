package com.ekuater.labelchat.command.tag;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.UserTag;

import org.json.JSONException;

/**
 * Created by Leo on 2015/3/15.
 *
 * @author LinYong
 */
public class GetTagCommand extends UserCommand {

    private static final String URL = CommandUrl.TAG_GET_TAG;

    public GetTagCommand() {
        super();
        setUrl(URL);
    }

    public GetTagCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamQueryUserId(String queryUserId) {
        putParam(CommandFields.Normal.QUERY_USER_ID, queryUserId);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public UserTag[] getTags() {
            return TagCmdUtils.toUserTagArray(getValueJsonArray(CommandFields.Tag.TAG_ARRAY));
        }
    }
}

package com.ekuater.labelchat.command;

import org.json.JSONException;

/**
 * Created by Leo on 2015/4/27.
 *
 * @author LinYong
 */
public class ComplainCommand extends UserCommand {

    private static final String URL = CommandUrl.MISC_COMPLAIN;

    public ComplainCommand() {
        super();
        setUrl(URL);
    }

    public ComplainCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamComplainType(String complainType) {
        putParam(CommandFields.Normal.COMPLAIN_TYPE, complainType);
    }

    public void putParamObjectId(String objectId) {
        putParam(CommandFields.Normal.OBJECT_ID, objectId);
    }

    public void putParamContent(String content) {
        putParam(CommandFields.Normal.CONTENT, content);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}

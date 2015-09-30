package com.ekuater.labelchat.command.interest;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.InterestType;

import org.json.JSONException;

/**
 * Created by Administrator on 2015/3/21.
 *
 * @author Xu wenxiang
 */
public class PushInteractCommand extends UserCommand {
    private static final String URL = CommandUrl.INTEREST_SEND_INTERACT;

    public PushInteractCommand() {
        super();
        setUrl(URL);
    }

    public PushInteractCommand(String sessionId, String userId) {
        super(sessionId, userId);
        setUrl(URL);
    }

    public void putParamInteractUserId(String interactUserId) {
        putParam(CommandFields.Interest.INTERACT_USERID, interactUserId);
    }

    public void putParamInteractType(String interactType) {
        putParam(CommandFields.Interest.INTERACT_TYPE, interactType);
    }

    public void putParamObjectType(String objectType) {
        putParam(CommandFields.Interest.OBJECT_TYPE, objectType);
    }

    public void putParamInteractObject(String interactObject) {
        putParam(CommandFields.Interest.INTERACT_OBJECT, interactObject);
    }

    public void putParamInteractOperate(String interactOperate) {
        putParam(CommandFields.Interest.INTERACT_OPERATE, interactOperate);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}
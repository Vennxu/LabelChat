package com.ekuater.labelchat.command.throwphoto;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.command.contact.ContactCmdUtils;
import com.ekuater.labelchat.datastruct.Stranger;

import org.json.JSONException;

/**
 * Created by Leo on 2015/1/6.
 *
 * @author LinYong
 */
public class PickCommand extends UserCommand {

    private static final String URL = CommandUrl.THROW_PHOTO_PICK;

    public PickCommand() {
        super();
        setUrl(URL);
    }

    public PickCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamThrowPhotoId(String throwPhotoId) {
        putParam(CommandFields.ThrowPhoto.THROW_PHOTO_ID, throwPhotoId);
    }

    public void putParamScenario(String scenario) {
        putParam(CommandFields.Normal.SCENARIO, scenario);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public Stranger getUserInfo() {
            return ContactCmdUtils.toStranger(getValueJson(
                    CommandFields.Stranger.STRANGER));
        }
    }
}

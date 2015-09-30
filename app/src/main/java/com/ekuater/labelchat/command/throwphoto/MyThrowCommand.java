package com.ekuater.labelchat.command.throwphoto;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.ThrowPhoto;

import org.json.JSONException;

/**
 * Created by Leo on 2015/1/6.
 * Get my throw photos from server
 *
 * @author LinYong
 */
public class MyThrowCommand extends UserCommand {

    private static final String URL = CommandUrl.THROW_PHOTO_MY_THROW;

    public MyThrowCommand() {
        super();
        setUrl(URL);
    }

    public MyThrowCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamQueryUserId(String userId) {
        putParam(CommandFields.Normal.QUERY_USER_ID, userId);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public ThrowPhoto[] getThrowPhotos() {
            return ThrowCmdUtils.toThrowPhotoArray(getValueJsonArray(
                    CommandFields.ThrowPhoto.THROW_PHOTO_ARRAY));
        }
    }
}

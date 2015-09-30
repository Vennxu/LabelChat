package com.ekuater.labelchat.command.album;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;

import org.json.JSONException;

/**
 * Created by Leo on 2015/3/24.
 *
 * @author LinYong
 */
public class PhotoNotifyCommand extends UserCommand {

    private static final String URL = CommandUrl.ALBUM_PHOTO_NOTIFY;

    public PhotoNotifyCommand() {
        super();
        setUrl(URL);
    }

    public PhotoNotifyCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamPhotoId(String photoId) {
        putParam(CommandFields.Album.PHOTO_ID, photoId);
    }

    public void putParamPhotoUserId(String photoUserId) {
        putParam(CommandFields.Album.PHOTO_USER_ID, photoUserId);
    }

    public void putParamPhotoNotifyType(String notifyType) {
        putParam(CommandFields.Album.PHOTO_NOTIFY_TYPE, notifyType);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }
}

package com.ekuater.labelchat.command.throwphoto;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.ThrowPhoto;

import org.json.JSONException;

/**
 * Created by Leo on 2015/1/6.
 *
 * @author LinYong
 */
public class MapShowCommand extends UserCommand {

    private static final String URL = CommandUrl.THROW_PHOTO_MAP_SHOW;

    public MapShowCommand() {
        super();
        setUrl(URL);
    }

    public MapShowCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void putParamLocation(LocationInfo location) {
        putParam(CommandFields.User.LONGITUDE, String.valueOf(location.getLongitude()));
        putParam(CommandFields.User.LATITUDE, String.valueOf(location.getLatitude()));
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

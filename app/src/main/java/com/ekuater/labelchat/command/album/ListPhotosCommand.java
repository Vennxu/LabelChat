package com.ekuater.labelchat.command.album;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.AlbumPhoto;

import org.json.JSONException;

/**
 * Created by Leo on 2015/3/19.
 *
 * @author LinYong
 */
public class ListPhotosCommand extends UserCommand {

    private static final String URL = CommandUrl.ALBUM_LIST_PHOTOS;

    public ListPhotosCommand() {
        super();
        setUrl(URL);
    }

    public ListPhotosCommand(String session, String userId) {
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

        public AlbumPhoto[] getPhotos() {
            return AlbumCmdUtils.toAlbumPhotoArray(getValueJsonArray(
                    CommandFields.Album.PHOTO_ARRAY));
        }
    }
}

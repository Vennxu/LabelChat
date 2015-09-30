package com.ekuater.labelchat.command.album;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UploadCommand;
import com.ekuater.labelchat.datastruct.AlbumPhoto;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by Leo on 2015/3/18.
 *
 * @author LinYong
 */
public class UploadPhotoCommand extends UploadCommand {

    private static final String URL = CommandUrl.ALBUM_UPLOAD_PHOTO;

    public UploadPhotoCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void addPhoto(File photo) throws FileNotFoundException {
        addFile(photo);
    }

    public void putParamRelatedUser(String userId) {
        putParam(CommandFields.Album.RELATED_USER_ID, userId);
    }

    public static class CommandResponse extends UploadCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public AlbumPhoto getUploadedPhoto() {
            return AlbumCmdUtils.toAlbumPhoto(getValueJson(CommandFields.Album.PHOTO_VO));
        }
    }
}

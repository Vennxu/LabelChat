package com.ekuater.labelchat.command.album;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.SessionCommand;
import com.ekuater.labelchat.command.contact.ContactCmdUtils;
import com.ekuater.labelchat.datastruct.LiteStranger;

import org.json.JSONException;

/**
 * Created by Leo on 2015/3/23.
 *
 * @author LinYong
 */
public class PhotoLikeUserCommand extends SessionCommand {

    private static final String URL = CommandUrl.ALBUM_PHOTO_LIKE_USER;

    public PhotoLikeUserCommand() {
        super();
        setUrl(URL);
    }

    public PhotoLikeUserCommand(String session) {
        super(session);
        setUrl(URL);
    }

    public void putParamPhotoId(String photoId) {
        putParam(CommandFields.Album.PHOTO_ID, photoId);
    }

    public static class CommandResponse extends SessionCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public LiteStranger[] geLikeUsers() {
            return ContactCmdUtils.toLiteStrangerArray(getValueJsonArray(
                    CommandFields.Stranger.STRANGERS));
        }
    }
}

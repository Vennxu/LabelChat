package com.ekuater.labelchat.command.account;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UploadCommand;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by Leo on 2015/4/12.
 *
 * @author LinYong
 */
public class UploadAvatarCommand extends UploadCommand {

    private static final String URL = CommandUrl.ACCOUNT_UPLOAD_AVATAR;

    public UploadAvatarCommand(String session, String userId) {
        super(session, userId);
        setUrl(URL);
    }

    public void setAvatarFile(File avatarFile) throws FileNotFoundException {
        clearFiles();
        addFile(avatarFile);
    }

    public static class CommandResponse extends UploadCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public String getAvatar() {
            return getValueString(CommandFields.User.AVATAR);
        }

        public String getAvatarThumb() {
            return getValueString(CommandFields.User.AVATAR_THUMB);
        }
    }
}

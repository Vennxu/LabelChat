package com.ekuater.labelchat.command.throwphoto;

import com.ekuater.labelchat.command.CommandErrorCode;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.command.contact.ContactCmdUtils;
import com.ekuater.labelchat.datastruct.Stranger;

import org.json.JSONException;

/**
 * Created by wenxiang on 2015/3/2.
 */
public class DeleteThrowCommand extends UserCommand {

    private static final String URL= CommandUrl.THROW_PHOTO_DELETE;

    public DeleteThrowCommand(){
        super();
        setUrl(URL);
    }

    public DeleteThrowCommand(String seesion,String userId){
        super(seesion,userId);
        setUrl(URL);
    }

    public void putParamThrowPhotoId(String throwPhotoId) {
        putParam(CommandFields.ThrowPhoto.THROW_PHOTO_ID, throwPhotoId);
    }
    public static class CommandResponse extends UserCommand.CommandResponse {
        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }

}

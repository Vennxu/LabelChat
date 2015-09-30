package com.ekuater.labelchat.command.contact;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.Stranger;

import org.json.JSONException;

/**
 * Created by wenxiang on 2015/3/3.
 */
public class NewUserCommand extends UserCommand {

    private static final String URL= CommandUrl.NEW_USER;

    public NewUserCommand(){
        super();
        setUrl(URL);
    }

    public NewUserCommand(String userId,String session){
        super(session,userId);
        setUrl(URL);
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public Stranger[] getNewUsers() {
            return ContactCmdUtils.toStrangerArray(
                    getValueJsonArray(CommandFields.Stranger.STRANGERS));
        }
    }

}



package com.ekuater.labelchat.command.confide;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.SessionCommand;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.Confide;

import org.json.JSONException;

/**
 * Created by Administrator on 2015/4/8.
 */
public class ConfideListCommand extends UserCommand{

    private static final String URL = CommandUrl.CONFIDE_LIST;

    public ConfideListCommand(){
        super();
        setUrl(URL);
    }

    public ConfideListCommand(String session, String userId){
        super(session, userId);
        setUrl(URL);
    }

    public void putParamRequstTime(String requestTime){
        putParam(CommandFields.Normal.REQUEST_TIME, requestTime);
    }

    public static class CommandResponse extends BaseCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public Confide[] getConfide(){
            return ConfideCmdUtils.toConfideArray(getValueJsonArray(CommandFields.Confide.CONFIDE_ARRAY));
        }
    }
}

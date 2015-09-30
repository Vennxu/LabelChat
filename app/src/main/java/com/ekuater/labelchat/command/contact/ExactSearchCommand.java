package com.ekuater.labelchat.command.contact;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.SessionCommand;
import com.ekuater.labelchat.datastruct.Stranger;

import org.json.JSONException;

/**
 * @author LinYong
 */
public class ExactSearchCommand extends SessionCommand {

    private static final String URL = CommandUrl.CONTACT_EXACT_SEARCH;

    public ExactSearchCommand(String session) {
        super(session);
        setUrl(URL);
    }

    public void putParamSearchWord(String searchWord) {
        putParam(CommandFields.Normal.SEARCH_WORD, searchWord);
    }

    public static class CommandResponse extends SessionCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public Stranger getUserInfo() {
            return ContactCmdUtils.toStranger(getValueJson(CommandFields.Stranger.STRANGER));
        }
    }
}

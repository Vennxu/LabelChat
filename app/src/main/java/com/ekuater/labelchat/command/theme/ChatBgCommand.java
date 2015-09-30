package com.ekuater.labelchat.command.theme;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.datastruct.ChatBg;

import org.json.JSONException;

/**
 * Created by Administrator on 2015/4/10.
 *
 * @author FanChong
 */
public class ChatBgCommand extends BaseCommand {

    private static final String URL = CommandUrl.CHAT_BACKGROUND_LIST;

    public ChatBgCommand() {
        super();
        setUrl(URL);
    }

    public static class CommandResponse extends BaseCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public ChatBg[] getChatBgs() {
            return ThemeCmdUtils.toChatBgArray(getValueJsonArray(
                    CommandFields.Theme.CHAT_BG_ARRAY));
        }
    }
}

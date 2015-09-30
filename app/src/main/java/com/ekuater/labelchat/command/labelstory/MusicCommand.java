package com.ekuater.labelchat.command.labelstory;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.command.labels.LabelCmdUtils;
import com.ekuater.labelchat.datastruct.Music;

import org.json.JSONException;

/**
 * Created by Administrator on 2015/5/26.
 *
 * @author FanChong
 */
public class MusicCommand extends UserCommand {
    private static final String URL = CommandUrl.MUSIC_SEARCH;

    public MusicCommand() {
        super();
        setUrl(URL);
    }

    public void putParamMusicName(String musicName) {
        putParam(CommandFields.Dynamic.KEY_WORD, musicName);
    }

    public void putParamRequestTime(int requestTime) {
        putParam(CommandFields.Normal.REQUEST_TIME, String.valueOf(requestTime));
    }

    public static class CommandResponse extends UserCommand.CommandResponse {

        public CommandResponse(String response) throws JSONException {
            super(response);
        }

        public Music[] getMusic() {
            return LabelCmdUtils.toMusicArray(getValueJsonArray(CommandFields.Dynamic.MUSIC_ARRAY));
        }

        public boolean isRemaining() {
            return getValueBoolean(CommandFields.Normal.REMAINING);
        }
    }
}

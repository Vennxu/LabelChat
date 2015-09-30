package com.ekuater.labelchat.command.mood;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.UserCommand;
import com.ekuater.labelchat.datastruct.MoodUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/5/12.
 *
 * @author Xu wenxiang
 */
public class MoodCommand extends UserCommand{

    private static final String URL = CommandUrl.MOOD_SEND;

    public MoodCommand(){
        super();
        setUrl(URL);
    }

    public MoodCommand(String session,String userId){
        super(session,userId);
        setUrl(URL);
    }

    public void putParamMood(String mood){
        putParam(CommandFields.Mood.MOOD, mood);
    }

    public void putParamArrayUserId(ArrayList<MoodUser> arrayUserId) {
        JSONArray jsonArray = new JSONArray();
        if (arrayUserId != null && arrayUserId.size() > 0) {
            for (MoodUser moodUser : arrayUserId) {
                jsonArray.put(moodUser.getUserId());
            }
        }
        putParam(CommandFields.Mood.RELATED_USER_IDS, jsonArray);
    }

    public static class CommandResponse extends UserCommand.CommandResponse{

        public CommandResponse(String response) throws JSONException {
            super(response);
        }
    }

}

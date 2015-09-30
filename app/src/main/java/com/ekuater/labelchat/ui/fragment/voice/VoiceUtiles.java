package com.ekuater.labelchat.ui.fragment.voice;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.datastruct.Music;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2015/4/15.
 */
public class VoiceUtiles {

    public static final String VOICE_URL = "voice";
    public static final int UPLOAD_VOICE_RESULT = 101;
    public static final int MSG_KEYBOARD_STATE_CHANGED = 102;

    public static String setContentJson(Music music, String content) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(CommandFields.Dynamic.SINGER_NAME, music.getSingerName());
            jsonObject.put(CommandFields.Dynamic.SONG_NAME,music.getSongName());
            jsonObject.put(CommandFields.Dynamic.DYNAMIC_CONTENT,content);
        } catch (JSONException e) {
            return null;
        }
        return jsonObject.toString();
    }

    public static JSONObject getContentJson(String json) {
        try {
            return json == null ? null:new JSONObject(json);
        } catch (JSONException e) {
            return null;
        }
    }

}

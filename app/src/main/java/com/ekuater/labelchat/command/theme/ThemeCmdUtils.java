package com.ekuater.labelchat.command.theme;

import android.text.TextUtils;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.datastruct.ChatBg;
import com.ekuater.labelchat.datastruct.UserTheme;
import com.ekuater.labelchat.util.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Leo on 2015/3/1.
 *
 * @author LinYong
 */
public class ThemeCmdUtils {

    private static final String TAG = ThemeCmdUtils.class.getSimpleName();

    public static UserTheme toTheme(JSONObject json) {
        if (json == null) {
            return null;
        }

        UserTheme theme = null;

        try {
            String themeName = json.getString(CommandFields.Theme.THEME);

            if (!TextUtils.isEmpty(themeName)) {
                theme = UserTheme.fromThemeName(themeName);
            }
        } catch (JSONException e) {
            L.w(TAG, e);
        }

        return theme;
    }

    public static UserTheme[] toThemeArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }

        ArrayList<UserTheme> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                UserTheme theme = toTheme(json);
                if (theme != null) {
                    list.add(theme);
                }
            }
        }

        final int size = list.size();
        return (size > 0) ? list.toArray(new UserTheme[size]) : null;
    }


    public static JSONObject toJson(ChatBg chatBg) {
        if (chatBg == null) {
            return null;
        }
        JSONObject json = null;
        try {
            json = new JSONObject();
            json.put(CommandFields.Theme.ID, chatBg.getId());
            json.put(CommandFields.Theme.BG_IMG, chatBg.getBgImg());
            json.put(CommandFields.Theme.BG_THUMB, chatBg.getBgThumb());
            json.put(CommandFields.Theme.SERIAL_NUM, chatBg.getSerialNum());
        } catch (JSONException e) {
            L.w(TAG, e);
        }
        return json;
    }


    public static ChatBg toChatBg(JSONObject json) {
        if (json == null) {
            return null;
        }

        ChatBg chatBg = null;

        try {
            int id = json.getInt(CommandFields.Theme.ID);
            String bgImg = json.getString(CommandFields.Theme.BG_IMG);
            String bgThumb = json.getString(CommandFields.Theme.BG_THUMB);
            int serialNum = json.getInt(CommandFields.Theme.SERIAL_NUM);
            chatBg = new ChatBg();

            chatBg.setId(id);
            chatBg.setBgImg(bgImg);
            chatBg.setBgThumb(bgThumb);
            chatBg.setSerialNum(serialNum);
        } catch (JSONException e) {
            L.w(TAG, e);
        }
        return chatBg;
    }

    public static ChatBg[] toChatBgArray(JSONArray jsonArray) {
        L.d(TAG, "toChatBgArray()---1");
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }
        L.d(TAG, "toChatBgArray()---2");

        ArrayList<ChatBg> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                ChatBg chatBg = toChatBg(json);
                if (chatBg != null) {
                    list.add(chatBg);
                    L.d(TAG, "toChatBgArray(), add");
                }
            }
        }

        final int size = list.size();
        return (size > 0) ? list.toArray(new ChatBg[size]) : null;
    }
}

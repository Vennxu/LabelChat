package com.ekuater.labelchat.command.following;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.FollowUser;
import com.ekuater.labelchat.util.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Leo on 2015/3/27.
 *
 * @author LinYong
 */
public class FollowCmdUtils {

    private static final String TAG = FollowCmdUtils.class.getSimpleName();

    public static FollowUser toFollowUser(JSONObject json) {
        if (json == null) {
            return null;
        }

        FollowUser user = null;

        try {
            String userId = json.getString(CommandFields.Stranger.USER_ID);
            String labelCode = json.getString(CommandFields.Stranger.LABEL_CODE);
            String nickname = json.getString(CommandFields.Stranger.NICKNAME);
            String avatarThumb = json.getString(CommandFields.Stranger.AVATAR_THUMB);
            String avatar = json.getString(CommandFields.Stranger.AVATAR);
            int gender = json.optInt(CommandFields.Stranger.SEX, ConstantCode.USER_SEX_MALE);

            user = new FollowUser();
            user.setUserId(userId);
            user.setLabelCode(labelCode);
            user.setNickname(nickname);
            user.setAvatar(avatar);
            user.setAvatarThumb(avatarThumb);
            user.setGender(gender);
        } catch (JSONException e) {
            L.w(TAG, e);
        }
        return user;
    }

    public static FollowUser[] toFollowUserArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }

        ArrayList<FollowUser> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                FollowUser user = toFollowUser(json);
                if (user != null) {
                    list.add(user);
                }
            }
        }

        final int size = list.size();
        return (size > 0) ? list.toArray(new FollowUser[size]) : null;
    }
}

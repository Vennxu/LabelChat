package com.ekuater.labelchat.command.confide;

import android.text.TextUtils;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.contact.ContactCmdUtils;
import com.ekuater.labelchat.datastruct.Confide;
import com.ekuater.labelchat.datastruct.ConfideComment;
import com.ekuater.labelchat.datastruct.ConfideRole;
import com.ekuater.labelchat.util.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Leo on 2015/4/7.
 *
 * @author LinYong
 */
public class ConfideCmdUtils {

    private static final String TAG = ConfideCmdUtils.class.getSimpleName();

    public static ConfideRole toRole(JSONObject json) {
        if (json == null) {
            return null;
        }

        ConfideRole role = null;

        try {
            int id = json.getInt(CommandFields.Confide.ROLE_ID);
            String name = json.getString(CommandFields.Confide.ROLE);

            role = new ConfideRole();
            role.setId(id);
            role.setName(name);
        } catch (JSONException e) {
            L.w(TAG, e);
        }
        return role;
    }

    public static Confide toConfide(JSONObject json) {
        if (json == null) {
            return null;
        }

        Confide confide = null;

        try {
            confide = new Confide();
            if (json.has(CommandFields.Confide.CONFIDE_ID)) {
                String confideId = json.getString(CommandFields.Confide.CONFIDE_ID);
                String confideUserId = json.getString(CommandFields.Confide.CONFIDE_USER_ID);
                String confideSex = json.optString(CommandFields.Confide.CONFIDE_SEX);
                String confideRole = json.optString(CommandFields.Confide.CONFIDE_ROLE);
                String confidePosition = json.optString(CommandFields.Confide.CONFIDE_POSITION);
                String confideBgColor = json.optString(CommandFields.Confide.CONFIDE_BG_COLOR);
                String confideBgImg = json.optString(CommandFields.Confide.BG_IMG);
                String confideContent = json.optString(CommandFields.Confide.CONFIDE_CONTENT);
                int confidePraiseNum = json.optInt(CommandFields.Confide.CONFIDE_PRAISE_NUM);
                int confideCommentNum = json.optInt(CommandFields.Confide.CONFIDE_COMMENT_NUM);
                String confideIsPraise = json.optString(CommandFields.Confide.CONFIDE_IS_PRAISE);
                long confideDate = json.optLong(CommandFields.Confide.CONFIDE_DATE);

                confide.setConfideId(confideId);
                confide.setConfideUserId(confideUserId);
                confide.setConfideSex(confideSex);
                confide.setConfideRole(confideRole);
                confide.setConfidePosition(confidePosition);
                confide.setConfideBgColor(confideBgColor);
                confide.setConfideBgImg(confideBgImg);
                confide.setConfideContent(confideContent);
                confide.setConfidePraiseNum(confidePraiseNum);
                confide.setConfideCommentNum(confideCommentNum);
                confide.setConfideIsPraise(confideIsPraise);
                confide.setConfideCreateDate(confideDate);
            }
            String commentArray = json.optString(CommandFields.Confide.CONFIDE_COMMENT_ARRAY);
            if (!TextUtils.isEmpty(commentArray)) {
                confide.setConfideComments(toConfideComment(new JSONArray(commentArray)));
            }

        } catch (JSONException e) {
            L.w(TAG, e);
        }
        return confide;
    }

    private static void mappingKey(JSONObject json, String[][] keyMap) throws JSONException {
        for (String[] item : keyMap) {
            if (json.has(item[0])) {
                json.put(item[1], json.remove(item[0]));
            }
        }
    }

    private static final String[][] STORY_KEY_MAP = new String[][]{
            {CommandFields.Dynamic.COMMENT_ID, CommandFields.Confide.CONFIDE_COMMENT_ID},
            {CommandFields.Dynamic.OBJECT_ID, CommandFields.Confide.CONFIDE_ID}
    };

    public static ConfideComment toConfideComment(JSONObject json) {
        if (json == null) {
            return null;
        }

        ConfideComment comment = null;

        try {
            String confideId = json.getString(CommandFields.Confide.CONFIDE_ID);
            String confideCommentId= json.getString(CommandFields.Confide.CONFIDE_COMMENT_ID);
            String confideUserId = json.optString(CommandFields.Confide.CONFIDE_USER_ID);
            String comments= json.optString(CommandFields.Confide.CONFIDE_COMMENT);
            String commentReplyComment = json.optString(CommandFields.Confide.CONFIDE_REPLY_COMMENT);
            String commentFloor = json.optString(CommandFields.Confide.CONFIDE_COMMENT_FLOOR);
            String commentReplyFloor = json.optString(CommandFields.Confide.CONFIDE_REPLY_FLOOR);
            String virtualAvata = json.optString(CommandFields.Confide.CONFIDE_VIRTUAL_AVATAR);
            String position = json.optString(CommandFields.Confide.CONFIDE_POSITION);
            long confideDate = json.optLong(CommandFields.Confide.CONFIDE_DATE);
            String userVo = json.optString(CommandFields.User.USER);

            comment = new ConfideComment();
            comment.setConfideId(confideId);
            comment.setConfideCommentId(confideCommentId);
            comment.setConfideUserId(confideUserId);
            comment.setComment(comments);
            comment.setReplyComment(commentReplyComment);
            comment.setFloor(commentFloor);
            comment.setReplyFloor(commentReplyFloor);
            comment.setVirtualAvatar(virtualAvata);
            comment.setPosition(position);
            comment.setCreateDate(confideDate);
            if (!TextUtils.isEmpty(userVo)) {
                comment.setStranger(ContactCmdUtils.toLiteStranger(new JSONObject(userVo)));
            }

        } catch (JSONException e) {
            L.w(TAG, e);
        }
        return comment;
    }

    public static ConfideComment[] toConfideComment(JSONArray jsonArray) throws JSONException {
        if (jsonArray == null || jsonArray.length() <= 0){
            return null;
        }

        ArrayList<ConfideComment> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length();++i){
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null){
                mappingKey(json, STORY_KEY_MAP);
                ConfideComment comment = toConfideComment(json);
                if (comment != null){
                    list.add(comment);
                }
            }
        }
        final int size = list.size();
        return (size > 0) ? list.toArray(new ConfideComment[size]):null;
    }

    public static ConfideRole[] toRoleArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }

        ArrayList<ConfideRole> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                ConfideRole role = toRole(json);
                if (role != null) {
                    list.add(role);
                }
            }
        }

        final int size = list.size();
        return (size > 0) ? list.toArray(new ConfideRole[size]) : null;
    }

    public static Confide[] toConfideArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }

        ArrayList<Confide> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                Confide confide = toConfide(json);
                if (confide != null) {
                    list.add(confide);
                }
            }
        }

        final int size = list.size();
        return (size > 0) ? list.toArray(new Confide[size]) : null;
    }
}

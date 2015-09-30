package com.ekuater.labelchat.command.interest;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.datastruct.InterestType;
import com.ekuater.labelchat.datastruct.RecentVisitor;
import com.ekuater.labelchat.datastruct.UserInterest;
import com.ekuater.labelchat.util.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/3/21.
 *
 * @author FanChong
 */
public class InterestCmdUtils {
    private static final String TAG = InterestCmdUtils.class.getSimpleName();

    public static UserInterest toUserInterest(JSONObject json) {
        if (json == null) {
            return null;
        }
        String interestId = json.optString(CommandFields.Interest.INTEREST_ID);
        String interestName = json.optString(CommandFields.Interest.INTEREST_NAME);
        int interestType = json.optInt(CommandFields.Interest.INTEREST_TYPE_ID);
        String interestTypeName = json.optString(CommandFields.Interest.INTEREST_TYPE_NAME);
        UserInterest interest = new UserInterest();
        interest.setInterestId(interestId);
        interest.setInterestName(interestName);
        interest.setInterestType(interestType);
        interest.setInterestTypeName(interestTypeName);

        return interest;
    }

    public static JSONObject toJson(UserInterest interest) {
        if (interest == null) {
            return null;
        }
        JSONObject json = null;
        try {
            json = new JSONObject();
            json.put(CommandFields.Interest.INTEREST_ID, interest.getInterestId());
            json.put(CommandFields.Interest.INTEREST_NAME, interest.getInterestName());
            json.put(CommandFields.Interest.INTEREST_TYPE_ID, interest.getInterestType());
        } catch (JSONException e) {
            L.w(TAG, e);
        }
        return json;
    }

    public static UserInterest[] toUserInterestArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }
        ArrayList<UserInterest> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                UserInterest interest = toUserInterest(json);
                if (interest != null) {
                    list.add(interest);
                }
            }
        }
        final int size = list.size();
        return (size > 0) ? list.toArray(new UserInterest[size]) : null;
    }

    public static JSONArray toJsonArray(UserInterest[] userInterests) {
        JSONArray jsonArray = null;
        if (userInterests != null && userInterests.length > 0) {
            ArrayList<JSONObject> list = new ArrayList<>();
            for (UserInterest interest : userInterests) {
                JSONObject json = toJson(interest);
                if (json != null) {
                    list.add(json);
                }
            }
            if (list.size() > 0) {
                jsonArray = new JSONArray(list);
            }
        }
        return jsonArray;

    }

    public static InterestType toInterestType(JSONObject json) {
        if (json == null) {
            return null;
        }
        InterestType interestType = null;
        try {
            int typeId = json.getInt(CommandFields.Interest.INTEREST_TYPE_ID);
            String typeName = json.getString(CommandFields.Interest.INTEREST_TYPE_NAME);
            UserInterest[] userInterests = toUserInterestArray(json.optJSONArray(CommandFields.Interest.INTEREST_ARRAY));
            interestType = new InterestType();
            interestType.setTypeId(typeId);
            interestType.setTypeName(typeName);
            interestType.setUserInterests(userInterests);
        } catch (JSONException e) {
            L.w(TAG, e);
        }
        return interestType;
    }

    public static InterestType[] toInterestTypeArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }
        ArrayList<InterestType> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                InterestType interestType = toInterestType(json);
                if (interestType != null) {
                    list.add(interestType);
                }
            }
        }
        final int size = list.size();
        return (size > 0) ? list.toArray(new InterestType[size]) : null;
    }

    public static RecentVisitor[] toRecentVisitorArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }
        ArrayList<RecentVisitor> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                RecentVisitor recentVisitor = toRecentVisitor(json);
                if (recentVisitor != null) {
                    list.add(recentVisitor);
                }
            }
        }
        final int size = list.size();
        return (size > 0) ? list.toArray(new RecentVisitor[size]) : null;
    }

    public static RecentVisitor toRecentVisitor(JSONObject json) {
        if (json == null) {
            return null;
        }
        RecentVisitor recentVisitor = null;
        try {
            String userId = json.getString(CommandFields.User.USER_ID);
            String userLabelCode = json.getString(CommandFields.User.LABEL_CODE);
            String userName = json.getString(CommandFields.User.NICKNAME);
            String userAvatar = json.getString(CommandFields.User.AVATAR);
            String userThumberAvatar = json.getString(CommandFields.User.AVATAR_THUMB);
            long date = json.getLong(CommandFields.User.DATE);
            recentVisitor = new RecentVisitor();
            recentVisitor.setRecentUserId(userId);
            recentVisitor.setRecentUserLabelCode(userLabelCode);
            recentVisitor.setRecentUserName(userName);
            recentVisitor.setRecentUserAvatar(userAvatar);
            recentVisitor.setRecentUserAvatarThumb(userThumberAvatar);
            recentVisitor.setRecentDate(date);
        } catch (JSONException e) {
            L.w(TAG, e);
        }
        return recentVisitor;
    }
}

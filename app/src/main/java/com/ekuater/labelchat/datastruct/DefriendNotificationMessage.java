package com.ekuater.labelchat.datastruct;

import android.text.TextUtils;

import com.ekuater.labelchat.util.L;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
public class DefriendNotificationMessage {

    private static final String TAG = DefriendNotificationMessage.class.getSimpleName();

    private String mLabelCode;
    private String mUserId;

    public DefriendNotificationMessage() {
    }

    public String getLabelCode() {
        return mLabelCode;
    }

    public void setLabelCode(String labelCode) {
        mLabelCode = labelCode;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public static DefriendNotificationMessage build(JSONObject json) {
        DefriendNotificationMessage newMessage = null;

        if (json != null) {
            String labelCode = json.optString(SystemPushFields.FIELD_LABEL_CODE);
            String userId = json.optString(SystemPushFields.FIELD_USER_ID);
            if (!TextUtils.isEmpty(labelCode) || !TextUtils.isEmpty(userId)) {
                newMessage = new DefriendNotificationMessage();
                newMessage.setLabelCode(labelCode);
                newMessage.setUserId(userId);
            }
        }

        return newMessage;
    }

    public static DefriendNotificationMessage build(SystemPush push) {
        DefriendNotificationMessage newMessage = null;

        if (push.getType() == SystemPushType.TYPE_DEFRIEND_NOTIFICATION) {
            try {
                newMessage = build(new JSONObject(push.getContent()));
            } catch (JSONException e) {
                L.w(TAG, e);
            }
        }

        return newMessage;
    }
}

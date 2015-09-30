package com.ekuater.labelchat.datastruct;

import android.text.TextUtils;

import com.ekuater.labelchat.util.L;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
public class TmpGroupMemberQuitMessage {

    public static final String TAG = TmpGroupMemberQuitMessage.class.getSimpleName();

    private String mGroupId;
    private String mUserId;
    private String mReason;

    public TmpGroupMemberQuitMessage() {
    }

    public String getGroupId() {
        return mGroupId;
    }

    public void setGroupId(String groupId) {
        mGroupId = groupId;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public String getReason() {
        return mReason;
    }

    public void setReason(String reason) {
        mReason = reason;
    }

    public static TmpGroupMemberQuitMessage build(JSONObject json) {
        TmpGroupMemberQuitMessage newMessage = null;

        if (json != null) {
            String groupId = json.optString(SystemPushFields.FIELD_GROUP_ID);
            String userId = json.optString(SystemPushFields.FIELD_USER_ID);
            String reason = json.optString(SystemPushFields.FIELD_REASON);

            if (!TextUtils.isEmpty(groupId) && !TextUtils.isEmpty(userId)) {
                newMessage = new TmpGroupMemberQuitMessage();
                newMessage.setGroupId(groupId);
                newMessage.setUserId(userId);
                newMessage.setReason(reason);
            }
        }

        return newMessage;
    }

    public static TmpGroupMemberQuitMessage build(SystemPush push) {
        TmpGroupMemberQuitMessage newMessage = null;

        if (push.getType() == SystemPushType.TYPE_TMP_GROUP_MEMBER_QUIT) {
            try {
                newMessage = build(new JSONObject(push.getContent()));
            } catch (JSONException e) {
                L.w(TAG, e);
            }
        }

        return newMessage;
    }
}

package com.ekuater.labelchat.datastruct;

import android.text.TextUtils;

import com.ekuater.labelchat.util.L;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
public class TmpGroupDismissMessage {

    private static final String TAG = TmpGroupDismissMessage.class.getSimpleName();

    private String mGroupId;
    private String mReason;

    public TmpGroupDismissMessage() {
    }

    public String getGroupId() {
        return mGroupId;
    }

    public void setGroupId(String groupId) {
        mGroupId = groupId;
    }

    public String getReason() {
        return mReason;
    }

    public void setReason(String reason) {
        mReason = reason;
    }

    public static TmpGroupDismissMessage build(JSONObject json) {
        TmpGroupDismissMessage newMessage = null;

        if (json != null) {
            String groupId = json.optString(SystemPushFields.FIELD_GROUP_ID);
            String reason = json.optString(SystemPushFields.FIELD_REASON);

            if (!TextUtils.isEmpty(groupId)) {
                newMessage = new TmpGroupDismissMessage();
                newMessage.setGroupId(groupId);
                newMessage.setReason(reason);
            }
        }

        return newMessage;
    }

    public static TmpGroupDismissMessage build(SystemPush push) {
        TmpGroupDismissMessage newMessage = null;

        if (push.getType() == SystemPushType.TYPE_TMP_GROUP_DISMISS) {
            try {
                newMessage = build(new JSONObject(push.getContent()));
            } catch (JSONException e) {
                L.w(TAG, e);
            }
        }

        return newMessage;
    }
}

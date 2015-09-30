package com.ekuater.labelchat.datastruct;

import android.text.TextUtils;

import com.ekuater.labelchat.util.L;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
public class TmpGroupDismissRemindMessage {

    private static final String TAG = TmpGroupDismissRemindMessage.class.getSimpleName();

    private String mGroupId;
    private long mTimeRemaining;

    public TmpGroupDismissRemindMessage() {
    }

    public String getGroupId() {
        return mGroupId;
    }

    public void setGroupId(String groupId) {
        mGroupId = groupId;
    }

    public long getTimeRemaining() {
        return mTimeRemaining;
    }

    public void setTimeRemaining(long timeRemaining) {
        mTimeRemaining = timeRemaining;
    }

    public static TmpGroupDismissRemindMessage build(JSONObject json) {
        TmpGroupDismissRemindMessage newMessage = null;

        if (json != null) {
            String groupId = json.optString(SystemPushFields.FIELD_GROUP_ID);
            long timeRemaining = json.optLong(SystemPushFields.FIELD_GROUP_TIME_REMAINING);

            if (!TextUtils.isEmpty(groupId) && timeRemaining > 0) {
                newMessage = new TmpGroupDismissRemindMessage();
                newMessage.setGroupId(groupId);
                newMessage.setTimeRemaining(timeRemaining);
            }
        }

        return newMessage;
    }

    public static TmpGroupDismissRemindMessage build(SystemPush push) {
        TmpGroupDismissRemindMessage newMessage = null;

        if (push.getType() == SystemPushType.TYPE_TMP_GROUP_DISMISS_REMIND) {
            try {
                newMessage = build(new JSONObject(push.getContent()));
            } catch (JSONException e) {
                L.w(TAG, e);
            }
        }

        return newMessage;
    }
}

package com.ekuater.labelchat.datastruct;

import com.ekuater.labelchat.util.L;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Local define push message, use to show temp group dismiss message for ui.
 *
 * @author LinYong
 */
public class LocalTmpGroupDismissedMessage extends LocalPushMessage {

    private static final String TAG = LocalTmpGroupDismissedMessage.class.getSimpleName();

    private String mGroupId;
    private String mGroupName;
    private String mGroupAvatar;
    private String mDismissMessage;
    private long mDismissTime;

    public LocalTmpGroupDismissedMessage() {
    }

    public String getGroupId() {
        return mGroupId;
    }

    public void setGroupId(String groupId) {
        mGroupId = groupId;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public void setGroupName(String groupName) {
        mGroupName = groupName;
    }

    public String getGroupAvatar() {
        return mGroupAvatar;
    }

    public void setGroupAvatar(String groupAvatar) {
        mGroupAvatar = groupAvatar;
    }

    public String getDismissMessage() {
        return mDismissMessage;
    }

    public void setDismissMessage(String dismissMessage) {
        mDismissMessage = dismissMessage;
    }

    public long getDismissTime() {
        return mDismissTime;
    }

    public void setDismissTime(long time) {
        mDismissTime = time;
    }

    @Override
    public SystemPush toSystemPush() {
        try {
            JSONObject json = new JSONObject();
            json.put(SystemPushFields.FIELD_GROUP_ID, getGroupId());
            json.put(SystemPushFields.FIELD_GROUP_NAME, getGroupName());
            json.put(SystemPushFields.FIELD_GROUP_AVATAR, getGroupAvatar());
            json.put(SystemPushFields.FIELD_GROUP_DISMISS_MESSAGE, getDismissMessage());
            json.put(SystemPushFields.FIELD_GROUP_DISMISS_TIME, getDismissTime());

            SystemPush systemPush = new SystemPush();
            systemPush.setId(-1L);
            systemPush.setState(SystemPush.STATE_UNPROCESSED);
            systemPush.setType(SystemPushType.TYPE_LOCAL_TMP_GROUP_DISMISSED);
            systemPush.setTime(System.currentTimeMillis());
            systemPush.setContent(json.toString());

            return systemPush;
        } catch (JSONException e) {
            L.w(TAG, e);
            return null;
        }
    }

    public static LocalTmpGroupDismissedMessage build(JSONObject json) {
        LocalTmpGroupDismissedMessage newMessage = null;

        if (json != null) {
            try {
                String groupId = json.getString(SystemPushFields.FIELD_GROUP_ID);
                String groupName = json.getString(SystemPushFields.FIELD_GROUP_NAME);
                String groupAvatar = json.getString(SystemPushFields.FIELD_GROUP_AVATAR);
                String dismissMessage = json.getString(SystemPushFields.FIELD_GROUP_DISMISS_MESSAGE);
                long dismissTime = json.getLong(SystemPushFields.FIELD_GROUP_DISMISS_TIME);

                newMessage = new LocalTmpGroupDismissedMessage();
                newMessage.setGroupId(groupId);
                newMessage.setGroupName(groupName);
                newMessage.setGroupAvatar(groupAvatar);
                newMessage.setDismissMessage(dismissMessage);
                newMessage.setDismissTime(dismissTime);
            } catch (JSONException e) {
                L.w(TAG, e);
            }

        }

        return newMessage;
    }

    public static LocalTmpGroupDismissedMessage build(SystemPush push) {
        LocalTmpGroupDismissedMessage newMessage = null;

        if (push.getType() == SystemPushType.TYPE_LOCAL_TMP_GROUP_DISMISSED) {
            try {
                newMessage = build(new JSONObject(push.getContent()));
            } catch (JSONException e) {
                e.printStackTrace();
                L.w(TAG, e);
            }
        }

        return newMessage;
    }
}

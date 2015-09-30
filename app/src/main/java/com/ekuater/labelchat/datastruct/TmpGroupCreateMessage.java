package com.ekuater.labelchat.datastruct;

import com.ekuater.labelchat.command.tmpgroup.TmpGroupCmdUtils;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
public class TmpGroupCreateMessage {

    private static final String TAG = TmpGroupCreateMessage.class.getSimpleName();

    private TmpGroup mGroup;

    public TmpGroupCreateMessage() {
    }

    public TmpGroup getGroup() {
        return mGroup;
    }

    public void setGroup(TmpGroup group) {
        mGroup = group;
    }

    public static TmpGroupCreateMessage build(JSONObject json) {
        TmpGroupCreateMessage newMessage = null;

        if (json != null) {
            TmpGroup group = TmpGroupCmdUtils.toTmpGroup(
                    json.optJSONObject(SystemPushFields.FIELD_GROUP));

            if (group != null) {
                newMessage = new TmpGroupCreateMessage();
                newMessage.setGroup(group);
            }
        }

        return newMessage;
    }

    public static TmpGroupCreateMessage build(SystemPush push) {
        TmpGroupCreateMessage newMessage = null;

        if (push.getType() == SystemPushType.TYPE_TMP_GROUP_CREATE) {
            try {
                newMessage = build(new JSONObject(push.getContent()));
            } catch (JSONException e) {
                L.w(TAG, e);
            }
        }

        return newMessage;
    }
}

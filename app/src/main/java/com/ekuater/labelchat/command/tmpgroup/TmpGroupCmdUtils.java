package com.ekuater.labelchat.command.tmpgroup;

import android.text.TextUtils;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.contact.ContactCmdUtils;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.TmpGroup;
import com.ekuater.labelchat.datastruct.TmpGroupTime;
import com.ekuater.labelchat.util.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
public class TmpGroupCmdUtils {

    private static final String TAG = TmpGroupCmdUtils.class.getSimpleName();

    public static TmpGroup toTmpGroup(JSONObject json) {
        if (json == null) {
            return null;
        }

        TmpGroup group = null;

        try {
            String groupId = json.getString(CommandFields.TmpGroup.GROUP_ID);
            String groupName = json.getString(CommandFields.TmpGroup.GROUP_NAME);
            String label = json.getString(CommandFields.TmpGroup.LABEL);
            String createUserId = json.getString(CommandFields.TmpGroup.CREATE_USER_ID);
            long createTime = json.getLong(CommandFields.TmpGroup.CREATE_TIME);
            long expireTime = json.getLong(CommandFields.TmpGroup.EXPIRE_TIME);
            long systemTime = json.optLong(CommandFields.TmpGroup.SYSTEM_TIME);
            String groupAvatar = json.optString(CommandFields.TmpGroup.GROUP_AVATAR);
            int state = json.getInt(CommandFields.TmpGroup.STATE);
            String memberValues = json.optString(CommandFields.TmpGroup.USER_ARRAY);

            TmpGroup tmpGroup = new TmpGroup();
            tmpGroup.setGroupId(groupId);
            tmpGroup.setGroupName(groupName);
            tmpGroup.setGroupLabelByString(label);
            tmpGroup.setCreateUserId(createUserId);
            tmpGroup.setCreateTime(createTime);
            tmpGroup.setExpireTime(expireTime);
            tmpGroup.setSystemTime(systemTime);
            tmpGroup.setGroupAvatar(groupAvatar);
            tmpGroup.setState(state);

            if (!TextUtils.isEmpty(memberValues)) {
                Stranger[] members = ContactCmdUtils.toStrangerArray(
                        new JSONArray(memberValues));
                if (members != null && members.length > 0) {
                    tmpGroup.setMembers(members);
                }
            }

            group = tmpGroup;
        } catch (JSONException e) {
            L.w(TAG, e);
        }

        return group;
    }

    public static TmpGroupTime toTmpGroupTime(JSONObject json) {
        if (json == null) {
            return null;
        }

        TmpGroupTime groupTime = null;

        try {
            String groupId = json.getString(CommandFields.TmpGroup.GROUP_ID);
            long createTime = json.getLong(CommandFields.TmpGroup.CREATE_TIME);
            long expireTime = json.getLong(CommandFields.TmpGroup.EXPIRE_TIME);
            long systemTime = json.getLong(CommandFields.TmpGroup.SYSTEM_TIME);

            TmpGroupTime tmpGroupTime = new TmpGroupTime();
            tmpGroupTime.setGroupId(groupId);
            tmpGroupTime.setCreateTime(createTime);
            tmpGroupTime.setExpireTime(expireTime);
            tmpGroupTime.setSystemTime(systemTime);

            groupTime = tmpGroupTime;
        } catch (JSONException e) {
            L.w(TAG, e);
        }

        return groupTime;
    }
}

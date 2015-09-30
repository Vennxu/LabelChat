package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

import com.ekuater.labelchat.command.labels.LabelCmdUtils;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * 24 hours life group information
 *
 * @author LinYong
 */
public class TmpGroup implements Parcelable {

    private static final String TAG = TmpGroup.class.getSimpleName();

    public static final int STATE_ACTIVE = 1;
    public static final int STATE_EXPIRED = 2;
    public static final int STATE_DISMISSED = 3;

    private String mGroupId;
    private String mGroupName;
    private SystemLabel mGroupLabel;
    private String mCreateUserId;
    private long mCreateTime;

    private long mExpireTime;
    private long mSystemTime;
    private long mLocalCreateTime;
    private long mDismissRemindTime;
    private String mGroupAvatar;
    private int mState;
    private Stranger[] mMembers;

    public TmpGroup() {
    }

    public String getGroupId() {
        return mGroupId;
    }

    public void setGroupId(String groupId) {
        this.mGroupId = groupId;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public void setGroupName(String groupName) {
        this.mGroupName = groupName;
    }

    public SystemLabel getGroupLabel() {
        return mGroupLabel;
    }

    public void setGroupLabel(SystemLabel groupLabel) {
        this.mGroupLabel = groupLabel;
    }

    public String getGroupLabelString() {
        return mGroupLabel != null ? LabelCmdUtils.toJson(mGroupLabel).toString() : "";
    }

    public void setGroupLabelByString(String label) {
        try {
            mGroupLabel = LabelCmdUtils.toSystemLabel(new JSONObject(label));
        } catch (JSONException e) {
            L.w(TAG, e);
            mGroupLabel = null;
        }
    }

    public String getCreateUserId() {
        return mCreateUserId;
    }

    public void setCreateUserId(String createUserId) {
        this.mCreateUserId = createUserId;
    }

    public long getCreateTime() {
        return mCreateTime;
    }

    public void setCreateTime(long createTime) {
        this.mCreateTime = createTime;
    }

    public long getExpireTime() {
        return mExpireTime;
    }

    public void setExpireTime(long expireTime) {
        this.mExpireTime = expireTime;
    }

    public long getSystemTime() {
        return mSystemTime;
    }

    public void setSystemTime(long systemTime) {
        this.mSystemTime = systemTime;
    }

    public long getLocalCreateTime() {
        return mLocalCreateTime;
    }

    public void setLocalCreateTime(long localCreateTime) {
        mLocalCreateTime = localCreateTime;
    }

    public long getDismissRemindTime() {
        return mDismissRemindTime;
    }

    public void setDismissRemindTime(long time) {
        mDismissRemindTime = time;
    }

    public String getGroupAvatar() {
        return mGroupAvatar;
    }

    public void setGroupAvatar(String groupAvatar) {
        this.mGroupAvatar = groupAvatar;
    }

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        this.mState = state;
    }

    public Stranger[] getMembers() {
        return mMembers;
    }

    public void setMembers(Stranger[] members) {
        this.mMembers = members;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mGroupId);
        dest.writeString(mGroupName);
        dest.writeParcelable(mGroupLabel, flags);
        dest.writeString(mCreateUserId);
        dest.writeLong(mCreateTime);
        dest.writeLong(mExpireTime);
        dest.writeLong(mSystemTime);
        dest.writeLong(mLocalCreateTime);
        dest.writeLong(mDismissRemindTime);
        dest.writeString(mGroupAvatar);
        dest.writeInt(mState);
        dest.writeParcelableArray(mMembers, flags);
    }

    public static final Parcelable.Creator<TmpGroup> CREATOR = new Parcelable.Creator<TmpGroup>() {

        @Override
        public TmpGroup createFromParcel(Parcel source) {
            TmpGroup instance = new TmpGroup();

            instance.mGroupId = source.readString();
            instance.mGroupName = source.readString();
            instance.mGroupLabel = source.readParcelable(SystemLabel.class.getClassLoader());
            instance.mCreateUserId = source.readString();
            instance.mCreateTime = source.readLong();
            instance.mExpireTime = source.readLong();
            instance.mSystemTime = source.readLong();
            instance.mLocalCreateTime = source.readLong();
            instance.mDismissRemindTime = source.readLong();
            instance.mGroupAvatar = source.readString();
            instance.mState = source.readInt();

            final Parcelable[] parcelables = source.readParcelableArray(
                    Stranger.class.getClassLoader());
            if (parcelables != null && parcelables.length > 0) {
                instance.mMembers = new Stranger[parcelables.length];
                for (int i = 0; i < parcelables.length; ++i) {
                    instance.mMembers[i] = (Stranger) parcelables[i];
                }
            } else {
                instance.mMembers = null;
            }

            return instance;
        }

        @Override
        public TmpGroup[] newArray(int size) {
            return new TmpGroup[size];
        }
    };

    @Override
    public String toString() {
        return "TmpGroup{" +
                "mGroupId='" + mGroupId + '\'' +
                ", mGroupName='" + mGroupName + '\'' +
                ", mGroupLabel=" + mGroupLabel +
                ", mCreateUserId='" + mCreateUserId + '\'' +
                ", mCreateTime=" + mCreateTime +
                ", mExpireTime=" + mExpireTime +
                ", mSystemTime=" + mSystemTime +
                ", mLocalCreateTime" + mLocalCreateTime +
                ", mDismissRemindTime" + mDismissRemindTime +
                ", mGroupAvatar='" + mGroupAvatar + '\'' +
                ", mState=" + mState +
                ", mMembers=" + Arrays.toString(mMembers) +
                '}';
    }
}

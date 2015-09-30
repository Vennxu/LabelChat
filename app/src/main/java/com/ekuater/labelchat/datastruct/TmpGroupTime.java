package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author LinYong
 */
public class TmpGroupTime implements Parcelable {

    private String mGroupId;
    private long mCreateTime;
    private long mExpireTime;
    private long mSystemTime;

    public TmpGroupTime() {
    }

    public String getGroupId() {
        return mGroupId;
    }

    public void setGroupId(String groupId) {
        this.mGroupId = groupId;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mGroupId);
        dest.writeLong(mCreateTime);
        dest.writeLong(mExpireTime);
        dest.writeLong(mSystemTime);
    }

    public static final Parcelable.Creator<TmpGroupTime> CREATOR
            = new Parcelable.Creator<TmpGroupTime>() {

        @Override
        public TmpGroupTime createFromParcel(Parcel source) {
            TmpGroupTime instance = new TmpGroupTime();

            instance.mGroupId = source.readString();
            instance.mCreateTime = source.readLong();
            instance.mExpireTime = source.readLong();
            instance.mSystemTime = source.readLong();

            return instance;
        }

        @Override
        public TmpGroupTime[] newArray(int size) {
            return new TmpGroupTime[size];
        }
    };
}

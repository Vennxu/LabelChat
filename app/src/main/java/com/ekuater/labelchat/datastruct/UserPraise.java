package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Label on 2015/1/23.
 *
 * @author Xu wenxiang
 */
public class UserPraise implements Parcelable {

    private String mPraiseUserId;
    private String mPraiseUserName;
    private String mPraiseUserAvatarThumb;
    private long mTime;

    public UserPraise() {

    }

    public UserPraise(String praiseUserId, String praiseUserName, String praiseUserAvatarThumb, long time) {
        mPraiseUserId = praiseUserId;
        mPraiseUserName = praiseUserName;
        mPraiseUserAvatarThumb = praiseUserAvatarThumb;
        mTime = time;

    }

    public String getmPraiseUserAvatarThumb() {
        return mPraiseUserAvatarThumb;
    }

    public void setmPraiseUserAvatarThumb(String mPraiseUserAvatarThumb) {
        this.mPraiseUserAvatarThumb = mPraiseUserAvatarThumb;
    }

    public String getmPraiseUserName() {
        return mPraiseUserName;
    }

    public void setmPraiseUserName(String mPraiseUserName) {
        this.mPraiseUserName = mPraiseUserName;
    }

    public String getmPraiseUserId() {
        return mPraiseUserId;
    }

    public void setmPraiseUserId(String mPraiseUserId) {
        this.mPraiseUserId = mPraiseUserId;
    }

    public long getmTime() {
        return mTime;
    }

    public void setmTime(long mTime) {
        this.mTime = mTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mPraiseUserId);
        dest.writeString(mPraiseUserName);
        dest.writeString(mPraiseUserAvatarThumb);
        dest.writeLong(mTime);
    }

    public static final Parcelable.Creator<UserPraise> CREATOR = new Parcelable.Creator<UserPraise>() {

        @Override
        public UserPraise createFromParcel(Parcel source) {
            UserPraise userPraise = new UserPraise();
            userPraise.mPraiseUserId = source.readString();
            userPraise.mPraiseUserName = source.readString();
            userPraise.mPraiseUserAvatarThumb = source.readString();
            userPraise.mTime = source.readLong();
            return userPraise;
        }

        @Override
        public UserPraise[] newArray(int size) {
            return new UserPraise[size];
        }
    };
}

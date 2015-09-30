package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Leo on 2015/2/5.
 *
 * @author LinYong
 */
public class UserLabelFeed implements Parcelable {

    private String userId;
    private String nickname;

    public UserLabelFeed(String userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
    }

    private UserLabelFeed(Parcel in) {
        this.userId = in.readString();
        this.nickname = in.readString();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userId);
        dest.writeString(this.nickname);
    }

    public static final Parcelable.Creator<UserLabelFeed> CREATOR
            = new Parcelable.Creator<UserLabelFeed>() {

        @Override
        public UserLabelFeed createFromParcel(Parcel source) {
            return new UserLabelFeed(source);
        }

        @Override
        public UserLabelFeed[] newArray(int size) {
            return new UserLabelFeed[size];
        }
    };
}

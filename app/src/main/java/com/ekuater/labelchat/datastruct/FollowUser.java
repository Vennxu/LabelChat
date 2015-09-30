package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Leo on 2015/3/27.
 *
 * @author LinYong
 */
public class FollowUser implements Parcelable {

    private String userId;
    private String labelCode;
    private String nickname;
    private String avatarThumb;
    private String avatar;
    private int gender;

    public FollowUser() {
    }

    public FollowUser(UserContact other) {
        this.userId = other.getUserId();
        this.labelCode = other.getLabelCode();
        this.nickname = other.getNickname();
        this.avatarThumb = other.getAvatarThumb();
        this.avatar = other.getAvatar();
        this.gender = other.getSex();
    }


    private FollowUser(Parcel in) {
        this.userId = in.readString();
        this.labelCode = in.readString();
        this.nickname = in.readString();
        this.avatarThumb = in.readString();
        this.avatar = in.readString();
        this.gender = in.readInt();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLabelCode() {
        return labelCode;
    }

    public void setLabelCode(String labelCode) {
        this.labelCode = labelCode;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarThumb() {
        return avatarThumb;
    }

    public void setAvatarThumb(String avatarThumb) {
        this.avatarThumb = avatarThumb;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userId);
        dest.writeString(this.labelCode);
        dest.writeString(this.nickname);
        dest.writeString(this.avatarThumb);
        dest.writeString(this.avatar);
        dest.writeInt(this.gender);
    }

    public static final Creator<FollowUser> CREATOR = new Creator<FollowUser>() {
        public FollowUser createFromParcel(Parcel source) {
            return new FollowUser(source);
        }

        public FollowUser[] newArray(int size) {
            return new FollowUser[size];
        }
    };
}

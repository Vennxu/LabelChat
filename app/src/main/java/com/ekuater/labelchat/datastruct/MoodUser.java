package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2015/5/9.
 */
public class MoodUser implements Parcelable {

    public static final int NO_SELECT = 0;
    public static final int SELECT = 1;

    private String userId;
    private String userName;
    private String avatarThumb;
    private String avatar;
    private int isSelect;

    public MoodUser(){

    }

    public MoodUser(UserContact contact, int isSelect){
        this.userId = contact.getUserId();
        this.userName = contact.getNickname();
        this.avatarThumb = contact.getAvatarThumb();
        this.avatar = contact.getAvatar();
        this.isSelect = isSelect;
    }

    public MoodUser(FollowUser followUser, int isSelect){
        this.userId = followUser.getUserId();
        this.userName = followUser.getNickname();
        this.avatarThumb = followUser.getAvatarThumb();
        this.avatar = followUser.getAvatar();
        this.isSelect = isSelect;
    }

    public MoodUser(Parcel source){
        this.userId = source.readString();
        this.userName = source.readString();
        this.avatarThumb = source.readString();
        this.avatar = source.readString();
        this.isSelect = source.readInt();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public int getIsSelect() {
        return isSelect;
    }

    public void setIsSelect(int isSelect) {
        this.isSelect = isSelect;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(userName);
        dest.writeString(avatarThumb);
        dest.writeString(avatar);
        dest.writeInt(isSelect);
    }

    public static Parcelable.Creator<MoodUser> CREATOR = new Parcelable.Creator<MoodUser>(){

        @Override
        public MoodUser createFromParcel(Parcel source) {
            return new MoodUser(source);
        }

        @Override
        public MoodUser[] newArray(int size) {
            return new MoodUser[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MoodUser moodUser = (MoodUser) o;

        return userId.equals(moodUser.userId);

    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }
}

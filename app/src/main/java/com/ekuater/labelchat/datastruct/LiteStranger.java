package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Leo on 2015/3/3.
 *
 * @author LinYong
 */
public class LiteStranger implements Parcelable {

    private String userId;
    private String labelCode;
    private String nickname;
    private String avatarThumb;
    private int gender;

    public LiteStranger() {
    }

    public LiteStranger(Stranger stranger) {
        this.userId = stranger.getUserId();
        this.labelCode = stranger.getLabelCode();
        this.nickname = stranger.getNickname();
        this.avatarThumb = stranger.getAvatarThumb();
        this.gender = stranger.getSex();
    }

    public LiteStranger(UserContact contact) {
        this.userId = contact.getUserId();
        this.labelCode = contact.getLabelCode();
        this.nickname = contact.getNickname();
        this.avatarThumb = contact.getAvatarThumb();
        this.gender = contact.getSex();
    }

    protected LiteStranger(Parcel in) {
        this.userId = in.readString();
        this.labelCode = in.readString();
        this.nickname = in.readString();
        this.avatarThumb = in.readString();
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
        dest.writeInt(this.gender);
    }

    public static final Parcelable.Creator<LiteStranger> CREATOR
            = new Parcelable.Creator<LiteStranger>() {
        public LiteStranger createFromParcel(Parcel source) {
            return new LiteStranger(source);
        }

        public LiteStranger[] newArray(int size) {
            return new LiteStranger[size];
        }
    };
}

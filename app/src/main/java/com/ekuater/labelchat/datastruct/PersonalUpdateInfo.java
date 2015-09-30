package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Use to store personal information to be update to server
 *
 * @author LinYong
 */
public class PersonalUpdateInfo implements Parcelable {

    private String avatar;
    private String avatarThumb;
    private String nickname;
    private int sex;
    private String province;
    private String city;
    private String school;
    private int constellation;
    private String signature;
    private String theme;

    public PersonalUpdateInfo() {
        avatar = null;
        nickname = null;
        sex = -1;
        province = null;
        city = null;
        school = null;
        constellation = -1;
        signature = null;
        theme = null;
    }

    private PersonalUpdateInfo(Parcel in) {
        this.avatar = in.readString();
        this.avatarThumb = in.readString();
        this.nickname = in.readString();
        this.sex = in.readInt();
        this.province = in.readString();
        this.city = in.readString();
        this.school = in.readString();
        this.constellation = in.readInt();
        this.signature = in.readString();
        this.theme = in.readString();
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatarThumb(String avatarThumb) {
        this.avatarThumb = avatarThumb;
    }

    public String getAvatarThumb() {
        return avatarThumb;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getSex() {
        return sex;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getProvince() {
        return province;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getSchool() {
        return school;
    }

    public void setConstellation(int constellation) {
        this.constellation = constellation;
    }

    public int getConstellation() {
        return constellation;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return signature;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(avatar);
        dest.writeString(avatarThumb);
        dest.writeString(nickname);
        dest.writeInt(sex);
        dest.writeString(province);
        dest.writeString(city);
        dest.writeString(school);
        dest.writeInt(constellation);
        dest.writeString(signature);
        dest.writeString(theme);
    }

    public static final Parcelable.Creator<PersonalUpdateInfo> CREATOR
            = new Parcelable.Creator<PersonalUpdateInfo>() {

        @Override
        public PersonalUpdateInfo createFromParcel(Parcel source) {
            return new PersonalUpdateInfo(source);
        }

        @Override
        public PersonalUpdateInfo[] newArray(int size) {
            return new PersonalUpdateInfo[size];
        }
    };
}

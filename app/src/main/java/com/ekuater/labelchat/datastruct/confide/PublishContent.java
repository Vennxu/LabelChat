package com.ekuater.labelchat.datastruct.confide;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Leo on 2015/4/9.
 *
 * @author LinYong
 */
public class PublishContent implements Parcelable {

    private String content;
    private String bgColor;
    private String bgImg;
    private String role;
    private int gender;
    private String position;

    public PublishContent() {
    }

    private PublishContent(Parcel in) {
        this.content = in.readString();
        this.bgColor = in.readString();
        this.bgImg = in.readString();
        this.role = in.readString();
        this.gender = in.readInt();
        this.position = in.readString();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getBgColor() {
        return bgColor;
    }

    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }

    public String getBgImg() {
        return bgImg;
    }

    public void setBgImg(String bgImg) {
        this.bgImg = bgImg;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.content);
        dest.writeString(this.bgColor);
        dest.writeString(this.bgImg);
        dest.writeString(this.role);
        dest.writeInt(this.gender);
        dest.writeString(this.position);
    }

    public static final Parcelable.Creator<PublishContent> CREATOR
            = new Parcelable.Creator<PublishContent>() {
        public PublishContent createFromParcel(Parcel source) {
            return new PublishContent(source);
        }

        public PublishContent[] newArray(int size) {
            return new PublishContent[size];
        }
    };
}

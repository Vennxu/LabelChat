package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2015/4/11.
 *
 * @author FanChong
 */
public class ChatBg implements Parcelable {

    private int id;
    private String bgImg;
    private String bgThumb;
    private int serialNum;

    public ChatBg() {
    }

    private ChatBg(Parcel in) {
        this.id = in.readInt();
        this.bgImg = in.readString();
        this.bgThumb = in.readString();
        this.serialNum = in.readInt();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBgImg() {
        return bgImg;
    }

    public void setBgImg(String bgImg) {
        this.bgImg = bgImg;
    }

    public String getBgThumb() {
        return bgThumb;
    }

    public void setBgThumb(String bgThumb) {
        this.bgThumb = bgThumb;
    }

    public int getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(int serialNum) {
        this.serialNum = serialNum;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.bgImg);
        dest.writeString(this.bgThumb);
        dest.writeInt(this.serialNum);
    }

    public static final Parcelable.Creator<ChatBg> CREATOR = new Creator<ChatBg>() {
        @Override
        public ChatBg createFromParcel(Parcel source) {
            return new ChatBg(source);
        }

        @Override
        public ChatBg[] newArray(int size) {
            return new ChatBg[size];
        }
    };
}

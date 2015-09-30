package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Leo on 2015/3/5.
 *
 * @author LinYong
 */
public class ChatRoom implements Parcelable {

    private String chatRoomId;
    private String chatRoomName;
    private int onlineCount;
    private String imageUrl;
    private String descript;

    public ChatRoom() {
    }

    private ChatRoom(Parcel in) {
        this.chatRoomId = in.readString();
        this.chatRoomName = in.readString();
        this.onlineCount = in.readInt();
        this.imageUrl = in.readString();
        this.descript = in.readString();
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getChatRoomName() {
        return chatRoomName;
    }

    public void setChatRoomName(String chatRoomName) {
        this.chatRoomName = chatRoomName;
    }

    public void setOnlineCount(int onlineCount){
        this.onlineCount = onlineCount;
    }

    public int getOnlineCount(){
        return onlineCount;
    }

    public void setImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }

    public String getImageUrl(){
        return imageUrl;
    }

    public void setDescript(String descript){
        this.descript = descript;
    }

    public String getDescript(){
        return descript;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.chatRoomId);
        dest.writeString(this.chatRoomName);
        dest.writeInt(this.onlineCount);
        dest.writeString(this.imageUrl);
        dest.writeString(this.descript);
    }

    public static final Parcelable.Creator<ChatRoom> CREATOR
            = new Parcelable.Creator<ChatRoom>() {
        public ChatRoom createFromParcel(Parcel source) {
            return new ChatRoom(source);
        }

        public ChatRoom[] newArray(int size) {
            return new ChatRoom[size];
        }
    };

}

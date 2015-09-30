package com.ekuater.labelchat.ui.fragment.push;

import android.os.Parcel;
import android.os.Parcelable;

import com.ekuater.labelchat.datastruct.Stranger;


/**
 * Created by Administrator on 2015/5/4.
 */
public class CommentPush implements Parcelable{

    public static final String DYNAMIC = "dynamic";
    public static final String CONFIDE = "confide";

    private int groupType;
    private String childType;
    private String reply;
    private Stranger stranger;
    private String flag;

    public CommentPush(){

    }

    public CommentPush(Parcel parcel){
        this.groupType = parcel.readInt();
        this.childType = parcel.readString();
        this.reply = parcel.readString();
        this.stranger = parcel.readParcelable(Stranger.class.getClassLoader());
        this.flag = parcel.readString();
    }

    public int getGroupType() {
        return groupType;
    }

    public void setGroupType(int groupType) {
        this.groupType = groupType;
    }

    public String getChildType() {
        return childType;
    }

    public void setChildType(String childType) {
        this.childType = childType;
    }

    public String getReply(){
        return reply;
    }

    public void setReply(String reply){
        this.reply = reply;
    }

    public Stranger getStranger() {
        return stranger;
    }

    public void setStranger(Stranger stranger) {
        this.stranger = stranger;
    }

    public String getFlag(){
        return flag;
    }

    public void setFlag(String flag){
        this.flag = flag;
    }

    public static final Parcelable.Creator<CommentPush> CREATOR= new Creator<CommentPush>() {
        @Override
        public CommentPush createFromParcel(Parcel source) {
            return new CommentPush(source);
        }

        @Override
        public CommentPush[] newArray(int size) {
            return new CommentPush[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(groupType);
        dest.writeString(childType);
        dest.writeString(reply);
        dest.writeParcelable(stranger, flags);
        dest.writeString(flag);
    }
}

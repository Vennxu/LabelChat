package com.ekuater.labelchat.ui.fragment.push;

import android.os.Parcel;
import android.os.Parcelable;

import com.ekuater.labelchat.datastruct.Stranger;

/**
 * Created by Administrator on 2015/4/30.
 */
public class RemaindPush implements Parcelable{

    public static final String PHOTO = "photo";
    public static final String INTERACT = "interact";
    public static final String INVITED = "invited";
    public static final String FOLLOW = "follow";
    public static final String NEW_PHOTO = "new_photo";
    public static final String CONFIDE_RECOMMEND= "confide_recommend";
    public static final String INTEREST = "interest";
    public static final String DYNAMIC = "dynamic";
    public static final String USERTAG = "usertag";

    private int groupType;
    private int remindFlag;
    private String remindType;
    private Stranger stranger;
    private String flag;



    public RemaindPush(){

    }

    public RemaindPush(Parcel parcel){
        this.groupType = parcel.readInt();
        this.remindFlag = parcel.readInt();
        this.remindType = parcel.readString();
        this.stranger = parcel.readParcelable(Stranger.class.getClassLoader());
        this.flag = parcel.readString();
    }

    public int getGroupType() {
        return groupType;
    }

    public void setGroupType(int groupType) {
        this.groupType = groupType;
    }

    public int getRemindFlag() {
        return remindFlag;
    }

    public void setRemindFlag(int remindFlag) {
        this.remindFlag = remindFlag;
    }

    public String getRemindType(){
        return remindType;
    }

    public void setRemindType(String remindType){
        this.remindType = remindType;
    }

    public Stranger getStranger() {
        return stranger;
    }

    public void setStranger(Stranger stranger) {
        this.stranger = stranger;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public static final Parcelable.Creator<PraisePush> CREATOR= new Parcelable.Creator<PraisePush>() {
        @Override
        public PraisePush createFromParcel(Parcel source) {
            return new PraisePush(source);
        }

        @Override
        public PraisePush[] newArray(int size) {
            return new PraisePush[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(groupType);
        dest.writeInt(remindFlag);
        dest.writeString(remindType);
        dest.writeParcelable(stranger, flags);
        dest.writeString(flag);
    }
}

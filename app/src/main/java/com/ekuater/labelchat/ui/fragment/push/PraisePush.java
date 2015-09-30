package com.ekuater.labelchat.ui.fragment.push;

import android.os.Parcel;
import android.os.Parcelable;

import com.ekuater.labelchat.datastruct.Stranger;


/**
 * Created by Administrator on 2015/5/4.
 */
public class PraisePush implements Parcelable{

    public static final String DYNAMIC = "dynamic";
    public static final String CONFIDE = "confide";
    public static final String PHOTO = "photo";

    private int groupType;
    private String childType;
    private Stranger stranger;
    private String flag;

    public PraisePush(){

    }

    public PraisePush(Parcel parcel){
        this.groupType = parcel.readInt();
        this.childType = parcel.readString();
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

    public static final Creator<PraisePush> CREATOR= new Creator<PraisePush>() {
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
        dest.writeString(childType);
        dest.writeParcelable(stranger, flags);
        dest.writeString(flag);
    }
}

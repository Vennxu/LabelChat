package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2015/4/29.
 */
public class Interact implements Parcelable{

    public static final int USER_TAG = 1;
    public static final int USER_INTEREST = 2;

    private int type;
    private UserTag userTag;
    private UserInterest userInterest;



    public Interact(){

    }

    public Interact(UserTag userTag){
        this.type = USER_TAG;
        this.userTag = userTag;
        this.userInterest = null;
    }

    public Interact(UserInterest userInterest){
        this.type = USER_INTEREST;
        this.userTag = null;
        this.userInterest = userInterest;
    }

    public Interact(Parcel source){
        this.type = source.readInt();
        this.userTag = source.readParcelable(UserTag.class.getClassLoader());
        this.userInterest = source.readParcelable(UserInterest.class.getClassLoader());
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public UserTag getUserTag() {
        return userTag;
    }

    public void setUserTag(UserTag userTag) {
        this.userTag = userTag;
    }

    public UserInterest getUserInterest() {
        return userInterest;
    }

    public void setUserInterest(UserInterest userInterest) {
        this.userInterest = userInterest;
    }

    public static final Creator<Interact> CREATOR = new Creator<Interact>() {
        @Override
        public Interact createFromParcel(Parcel source) {
            return new Interact(source);
        }

        @Override
        public Interact[] newArray(int size) {
            return new Interact[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeParcelable(this.userTag, flags);
        dest.writeParcelable(this.userInterest, flags);
    }
}

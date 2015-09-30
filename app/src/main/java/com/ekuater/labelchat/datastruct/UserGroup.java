package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2015/5/11.
 */
public class UserGroup implements Parcelable{

    public static final int NO_SELECT = 0;
    public static final int ALL_SELECT = 1;

    private int isAllSelect;
    private String groupName;

    public UserGroup(){

    }

    public UserGroup(int isAllSelect, String groupName){
        this.isAllSelect = isAllSelect;
        this.groupName = groupName;
    }

    public UserGroup(Parcel parcel){
        this.isAllSelect = parcel.readInt();
        this.groupName = parcel.readString();
    }

    public int isAllSelect() {
        return isAllSelect;
    }

    public void setIsAllSelect(int isAllSelect) {
        this.isAllSelect = isAllSelect;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(isAllSelect);
        dest.writeString(groupName);
    }

    public static Parcelable.Creator<UserGroup> CREATOR = new Parcelable.Creator<UserGroup>(){

        @Override
        public UserGroup createFromParcel(Parcel source) {
            return new UserGroup(source);
        }

        @Override
        public UserGroup[] newArray(int size) {
            return new UserGroup[size];
        }
    };
}

package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Leo on 2015/4/7.
 *
 * @author LinYong
 */
public class ConfideRole implements Parcelable {

    private int id;
    private String name;

    public ConfideRole() {
    }

    private ConfideRole(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
    }

    public static final Parcelable.Creator<ConfideRole> CREATOR
            = new Parcelable.Creator<ConfideRole>() {
        public ConfideRole createFromParcel(Parcel source) {
            return new ConfideRole(source);
        }

        public ConfideRole[] newArray(int size) {
            return new ConfideRole[size];
        }
    };
}

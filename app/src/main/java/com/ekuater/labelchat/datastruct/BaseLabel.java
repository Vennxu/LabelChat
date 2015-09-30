package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

import com.ekuater.labelchat.command.labels.LabelCmdUtils;

/**
 * @author LinYong
 */
public class BaseLabel implements Parcelable {

    private String name;
    private String id;

    public BaseLabel(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public BaseLabel(BaseLabel other) {
        this.name = other.name;
        this.id = other.id;
    }

    private BaseLabel(Parcel in) {
        this.name = in.readString();
        this.id = in.readString();
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(id);
    }

    public static final Parcelable.Creator<BaseLabel> CREATOR
            = new Parcelable.Creator<BaseLabel>() {

        @Override
        public BaseLabel createFromParcel(Parcel source) {
            return new BaseLabel(source);
        }

        @Override
        public BaseLabel[] newArray(int size) {
            return new BaseLabel[size];
        }
    };

    @Override
    public String toString() {
        return LabelCmdUtils.toJson(this).toString();
    }
}

package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.ekuater.labelchat.command.labels.LabelCmdUtils;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
public class SystemLabel implements Parcelable, Comparable<SystemLabel> {

    private static final String TAG = SystemLabel.class.getSimpleName();

    private String name;
    private String id;
    private String createUserId;
    private long time;
    private long totalUser;
    private String image;

    public SystemLabel(String name, String id, String createUserId,
                       long time, long totalUser) {
        this.name = name;
        this.id = id;
        this.createUserId = createUserId;
        this.time = time;
        this.totalUser = totalUser;
    }

    public SystemLabel(SystemLabel other) {
        this.name = other.name;
        this.id = other.id;
        this.createUserId = other.createUserId;
        this.time = other.time;
        this.totalUser = other.totalUser;
        this.image = other.image;
    }

    private SystemLabel(Parcel in) {
        this.name = in.readString();
        this.id = in.readString();
        this.createUserId = in.readString();
        this.time = in.readLong();
        this.totalUser = in.readLong();
        this.image = in.readString();
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getCreateUserId() {
        return createUserId;
    }

    public long getTime() {
        return time;
    }

    public long getTotalUser() {
        return totalUser;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public BaseLabel toBaseLabel() {
        return new BaseLabel(name, id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(id);
        dest.writeString(createUserId);
        dest.writeLong(time);
        dest.writeLong(totalUser);
        dest.writeString(image);
    }

    public static final Parcelable.Creator<SystemLabel> CREATOR
            = new Parcelable.Creator<SystemLabel>() {

        @Override
        public SystemLabel createFromParcel(Parcel source) {
            return new SystemLabel(source);
        }

        @Override
        public SystemLabel[] newArray(int size) {
            return new SystemLabel[size];
        }
    };

    @Override
    public int compareTo(@NonNull SystemLabel another) {
        return (int) (time - another.time);
    }

    @Override
    public String toString() {
        return LabelCmdUtils.toJson(this).toString();
    }

    public static SystemLabel build(String labelString) {
        SystemLabel label = null;

        if (!TextUtils.isEmpty(labelString)) {
            try {
                JSONObject json = new JSONObject(labelString);
                label = LabelCmdUtils.toSystemLabel(json);
            } catch (JSONException e) {
                L.w(TAG, e);
            }
        }

        return label;
    }
}

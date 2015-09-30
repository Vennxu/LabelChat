package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

import com.ekuater.labelchat.R;

/**
 * Created by Administrator on 2015/3/21.
 *
 * @author FanChong
 */
public class InterestType implements Parcelable {

    public static final int MOVIE = 1;
    public static final int MUSIC = 2;
    public static final int BOOK = 3;
    public static final int SPORT = 4;
    public static final int FOOD = 5;
    public static final int COUNT = 5;


    private int typeId;
    private String typeName;
    private UserInterest[] userInterests;

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public UserInterest[] getUserInterests() {
        return userInterests;
    }

    public void setUserInterests(UserInterest[] userInterests) {
        this.userInterests = userInterests;
    }


    public InterestType() {

    }

    public InterestType(Parcel in) {
        this.typeId = in.readInt();
        this.typeName = in.readString();
        Parcelable[] parcelables = in.readParcelableArray(UserInterest.class.getClassLoader());
        if (parcelables != null) {
            this.userInterests = new UserInterest[parcelables.length];
            for (int i = 0; i < parcelables.length; i++) {
                this.userInterests[i] = (UserInterest) parcelables[i];
            }
        } else {
            this.userInterests = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.typeId);
        dest.writeString(this.typeName);
        dest.writeParcelableArray(this.userInterests, flags);
    }
public static final Parcelable.Creator<InterestType> CREATOR= new Creator<InterestType>() {
    @Override
    public InterestType createFromParcel(Parcel source) {
        return new InterestType(source);
    }

    @Override
    public InterestType[] newArray(int size) {
        return new InterestType[size];
    }
};
}

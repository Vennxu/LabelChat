package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2015/3/21.
 *
 * @author FanChong
 */
public class UserInterest implements Parcelable {
    private String interestId;
    private String interestName;
    private int interestType;
    private String interestTypeName;

    public String getinterestTypeName() {
        return interestTypeName;
    }

    public void setInterestTypeName(String interestTypeName) {
        this.interestTypeName = interestTypeName;
    }

    public String getInterestId() {
        return interestId;
    }

    public void setInterestId(String interestId) {
        this.interestId = interestId;
    }

    public int getInterestType() {
        return interestType;
    }

    public void setInterestType(int interestType) {
        this.interestType = interestType;
    }

    public String getInterestName() {
        return interestName;
    }

    public void setInterestName(String interestName) {
        this.interestName = interestName;
    }

    public UserInterest() {
    }

    public UserInterest(Parcel in) {
        this.interestId = in.readString();
        this.interestName = in.readString();
        this.interestType = in.readInt();
        this.interestTypeName = in.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.interestId);
        dest.writeString(this.interestName);
        dest.writeInt(this.interestType);
        dest.writeString(this.interestTypeName);
    }

    public static final Parcelable.Creator<UserInterest> CREATOR = new Parcelable.Creator<UserInterest>() {
        @Override
        public UserInterest createFromParcel(Parcel source) {
            return new UserInterest(source);
        }

        @Override
        public UserInterest[] newArray(int size) {
            return new UserInterest[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserInterest that = (UserInterest) o;

        return interestName.equals(that.interestName);

    }

    @Override
    public int hashCode() {
        return interestName.hashCode();
    }
}

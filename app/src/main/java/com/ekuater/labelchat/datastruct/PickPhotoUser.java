package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2015/2/7.
 * @author FanChong
 */
public class PickPhotoUser implements Parcelable {
    private String pickUserId;
    private String pickUserName;
    private String pickUserAvatarThumb;
    private long pickPhotoDate;
    private int gradeNum;

    @Override
    public String toString() {
        return "PickPhotoUser{" +
                "pickUserId='" + pickUserId + '\'' +
                ", pickUserName='" + pickUserName + '\'' +
                ", pickUserAvatarThumb='" + pickUserAvatarThumb + '\'' +
                ", pickPhotoDate=" + pickPhotoDate +
                ", gradeNum=" + gradeNum +
                '}';
    }

    public PickPhotoUser() {
    }

    public String getPickUserName() {
        return pickUserName;
    }

    public void setPickUserName(String pickUserName) {
        this.pickUserName = pickUserName;
    }

    public String getPickUserAvatarThumb() {
        return pickUserAvatarThumb;
    }

    public void setPickUserAvatarThumb(String pickUserAvatarThumb) {
        this.pickUserAvatarThumb = pickUserAvatarThumb;
    }

    public String getPickUserId() {
        return pickUserId;
    }

    public void setPickUserId(String pickUserId) {
        this.pickUserId = pickUserId;
    }

    public long getPickPhotoDate() {
        return pickPhotoDate;
    }

    public void setPickPhotoDate(long pickPhotoDate) {
        this.pickPhotoDate = pickPhotoDate;
    }

    public int getGradeNum() {
        return gradeNum;
    }

    public void setGradeNum(int gradeNum) {
        this.gradeNum = gradeNum;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getPickUserId());
        dest.writeString(getPickUserName());
        dest.writeString(getPickUserAvatarThumb());
        dest.writeLong(getPickPhotoDate());
        dest.writeInt(getGradeNum());
    }

    public static final Parcelable.Creator<PickPhotoUser> CREATOR = new Parcelable.Creator<PickPhotoUser>() {
        @Override
        public PickPhotoUser createFromParcel(Parcel source) {
            PickPhotoUser pickPhoto = new PickPhotoUser();
            pickPhoto.pickUserId = source.readString();
            pickPhoto.pickUserName = source.readString();
            pickPhoto.pickUserAvatarThumb = source.readString();
            pickPhoto.pickPhotoDate = source.readLong();
            pickPhoto.gradeNum = source.readInt();
            return pickPhoto;
        }

        @Override
        public PickPhotoUser[] newArray(int size) {
            return new PickPhotoUser[size];
        }
    };
}

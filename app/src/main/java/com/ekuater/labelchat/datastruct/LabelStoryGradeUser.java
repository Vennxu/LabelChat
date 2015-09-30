package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Created by Label on 2015/2/12.
 */
public class LabelStoryGradeUser implements Parcelable{

    private String mUserId;
    private String mNickName;
    private String mAvataThumb;
    private long mCreateDate;
    private int mStoryGrade;


    public int getmStoryGrade() {
        return mStoryGrade;
    }

    public void setmStoryGrade(int mStoryGrade) {
        this.mStoryGrade = mStoryGrade;
    }

    public String getmNickName() {
        return mNickName;
    }

    public void setmNickName(String mNickName) {
        this.mNickName = mNickName;
    }

    public String getmUserId() {
        return mUserId;
    }

    public void setmUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public String getmAvataThumb() {
        return mAvataThumb;
    }

    public void setmAvataThumb(String mAvataThumb) {
        this.mAvataThumb = mAvataThumb;
    }

    public long getmCreateDate() {
        return mCreateDate;
    }

    public void setmCreateDate(long mCreateDate) {
        this.mCreateDate = mCreateDate;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUserId);
        dest.writeString(mNickName);
        dest.writeString(mAvataThumb);
        dest.writeLong(mCreateDate);
        dest.writeInt(mStoryGrade);
    }
    public static final Parcelable.Creator<LabelStoryGradeUser> CREATOR=new Parcelable.Creator<LabelStoryGradeUser>() {
        @Override
        public LabelStoryGradeUser createFromParcel(Parcel source) {
            LabelStoryGradeUser labelStoryGradeUser=new LabelStoryGradeUser();
            labelStoryGradeUser.mUserId=source.readString();
            labelStoryGradeUser.mNickName=source.readString();
            labelStoryGradeUser.mAvataThumb=source.readString();
            labelStoryGradeUser.mCreateDate=source.readLong();
            labelStoryGradeUser.mStoryGrade=source.readInt();
            return labelStoryGradeUser;
        }

        @Override
        public LabelStoryGradeUser[] newArray(int size) {
            return new LabelStoryGradeUser[0];
        }
    };
}

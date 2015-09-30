package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Label on 2015/3/11.
 */
public class LabelStoryCategory implements Parcelable{

    private String mCategoryId;
    private String mCategoryName;
    private int mDynamicTotal;
    private int mSerialNum;
    private String mImageUrl;


    public String getmCategoryId() {
        return mCategoryId;
    }

    public void setmCategoryId(String mCategoryId) {
        this.mCategoryId = mCategoryId;
    }

    public String getmCategoryName() {
        return mCategoryName;
    }

    public void setmCategoryName(String mCategoryName) {
        this.mCategoryName = mCategoryName;
    }

    public int getmDynamicTotal() {
        return mDynamicTotal;
    }

    public void setmDynamicTotal(int mDynamicTotal) {
        this.mDynamicTotal = mDynamicTotal;
    }

    public int getmSerialNum() {
        return mSerialNum;
    }

    public void setmSerialNum(int mSerialNum) {
        this.mSerialNum = mSerialNum;
    }

    public String getmImageUrl(){
        return mImageUrl;
    }

    public void setmImageUrl(String mImageUrl){
        this.mImageUrl = mImageUrl;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
         dest.writeString(mCategoryId);
         dest.writeString(mCategoryName);
         dest.writeInt(mDynamicTotal);
         dest.writeInt(mSerialNum);
         dest.writeString(mImageUrl);
    }

    public static final Parcelable.Creator<LabelStoryCategory> CREATOR = new Parcelable.Creator<LabelStoryCategory>(){

        @Override
        public LabelStoryCategory createFromParcel(Parcel source) {
            LabelStoryCategory labelStoryCategory = new LabelStoryCategory();
            labelStoryCategory.mCategoryId = source.readString();
            labelStoryCategory.mCategoryName = source.readString();
            labelStoryCategory.mDynamicTotal = source.readInt();
            labelStoryCategory.mSerialNum = source.readInt();
            labelStoryCategory.mImageUrl = source.readString();
            return labelStoryCategory;
        }

        @Override
        public LabelStoryCategory[] newArray(int size) {
            return new LabelStoryCategory[0];
        }
    };
}

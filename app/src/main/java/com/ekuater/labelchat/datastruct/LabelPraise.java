package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Leo on 2015/1/14.
 *
 * @author LinYong
 */
public class LabelPraise implements Parcelable {

    private String userId;
    private String labelId;
    private int praiseCount;

    private LabelPraise() {
    }

    public LabelPraise(String userId, String labelId) {
        this(userId, labelId, 0);
    }

    public LabelPraise(String userId, String labelId, int praiseCount) {
        this.userId = userId;
        this.labelId = labelId;
        this.praiseCount = praiseCount;
    }

    public String getUserId() {
        return userId;
    }

    public String getLabelId() {
        return labelId;
    }

    public int getPraiseCount() {
        return praiseCount;
    }

    public void setPraiseCount(int praiseCount) {
        this.praiseCount = praiseCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(labelId);
        dest.writeInt(praiseCount);
    }

    public static final Parcelable.Creator<LabelPraise> CREATOR
            = new Parcelable.Creator<LabelPraise>() {

        @Override
        public LabelPraise createFromParcel(Parcel source) {
            LabelPraise instance = new LabelPraise();
            instance.userId = source.readString();
            instance.labelId = source.readString();
            instance.praiseCount = source.readInt();
            return instance;
        }

        @Override
        public LabelPraise[] newArray(int size) {
            return new LabelPraise[size];
        }
    };
}

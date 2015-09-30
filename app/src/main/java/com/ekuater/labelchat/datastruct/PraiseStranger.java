package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2015/1/17.
 *
 * @author LinYong
 */
public class PraiseStranger implements Parcelable {

    private Stranger stranger;
    private String praiseLabelId;
    private int praiseCount;

    private PraiseStranger() {
    }

    public PraiseStranger(Stranger stranger, String praiseLabelId, int praiseCount) {
        this.stranger = stranger;
        this.praiseLabelId = praiseLabelId;
        this.praiseCount = praiseCount;
    }

    public Stranger getStranger() {
        return stranger;
    }

    public String getPraiseLabelId() {
        return praiseLabelId;
    }

    public int getPraiseCount() {
        return praiseCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(stranger, flags);
        dest.writeString(praiseLabelId);
        dest.writeInt(praiseCount);
    }

    public static final Parcelable.Creator<PraiseStranger> CREATOR
            = new Parcelable.Creator<PraiseStranger>() {

        @Override
        public PraiseStranger createFromParcel(Parcel source) {
            PraiseStranger instance = new PraiseStranger();
            instance.stranger = source.readParcelable(Stranger.class.getClassLoader());
            instance.praiseLabelId = source.readString();
            instance.praiseCount = source.readInt();
            return null;
        }

        @Override
        public PraiseStranger[] newArray(int size) {
            return new PraiseStranger[size];
        }
    };
}

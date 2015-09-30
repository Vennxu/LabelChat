package com.ekuater.labelchat.datastruct.mixdynamic;

import android.os.Parcel;
import android.os.Parcelable;

import com.ekuater.labelchat.datastruct.Confide;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.ParcelUtils;

/**
 * Created by Leo on 2015/4/16.
 *
 * @author LinYong
 */
public class DynamicWrapper implements Parcelable {

    private String objectId;
    private long time;
    private DynamicType type;
    private Parcelable dynamic;

    public DynamicWrapper() {
    }

    private DynamicWrapper(Parcel in) {
        this.objectId = in.readString();
        this.time = in.readLong();
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : DynamicType.values()[tmpType];
        this.dynamic = readStoryFromParcel(in, this.type);
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public DynamicType getType() {
        return type;
    }

    public void setType(DynamicType type) {
        this.type = type;
    }

    public Parcelable getDynamic() {
        return dynamic;
    }

    public void setDynamic(Parcelable dynamic) {
        this.dynamic = dynamic;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.objectId);
        dest.writeLong(this.time);
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
        ParcelUtils.writeParcelType(dest, this.dynamic, flags);
    }

    public static final Parcelable.Creator<DynamicWrapper> CREATOR
            = new Parcelable.Creator<DynamicWrapper>() {
        public DynamicWrapper createFromParcel(Parcel source) {
            return new DynamicWrapper(source);
        }

        public DynamicWrapper[] newArray(int size) {
            return new DynamicWrapper[size];
        }
    };

    private static Parcelable readStoryFromParcel(Parcel in, DynamicType type) {
        final Parcelable story;

        switch (type) {
            case TXT:
            case AUDIO:
            case BANKNOTE:
            case ONLINEAUDIO:
                story = ParcelUtils.createParcelType(in, LabelStory.CREATOR);
                break;
            case CONFIDE:
                story = ParcelUtils.createParcelType(in, Confide.CREATOR);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported DynamicType!");
        }
        return story;
    }
}

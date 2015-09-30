package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Leo on 2015/3/15.
 *
 * @author LinYong
 */
public class TagType implements Parcelable {

    private int typeId;
    private String typeName;
    private int maxSelect;
    private UserTag[] tags;

    public TagType() {
    }

    private TagType(Parcel in) {
        this.typeId = in.readInt();
        this.typeName = in.readString();
        this.maxSelect = in.readInt();
        this.tags = in.createTypedArray(UserTag.CREATOR);
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getMaxSelect() {
        return maxSelect;
    }

    public void setMaxSelect(int maxSelect) {
        this.maxSelect = maxSelect;
    }

    public UserTag[] getTags() {
        return tags;
    }

    public void setTags(UserTag[] tags) {
        this.tags = tags;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.typeId);
        dest.writeString(this.typeName);
        dest.writeInt(this.maxSelect);
        dest.writeTypedArray(this.tags, flags);
    }

    public static final Parcelable.Creator<TagType> CREATOR = new Parcelable.Creator<TagType>() {
        public TagType createFromParcel(Parcel source) {
            return new TagType(source);
        }

        public TagType[] newArray(int size) {
            return new TagType[size];
        }
    };
}

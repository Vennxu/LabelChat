package com.ekuater.labelchat.datastruct;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.ekuater.labelchat.util.ColorUtils;
import com.ekuater.labelchat.util.L;

/**
 * Created by Leo on 2015/3/15.
 *
 * @author LinYong
 */
public class UserTag implements Parcelable {

    private static final String TAG = UserTag.class.getSimpleName();
    private static final int DEFAULT_TAG_COLOR = Color.WHITE;
    private static final int DEFAULT_TAG_SELECTED_COLOR = Color.LTGRAY;

    private int tagId;
    private String tagName;
    private String tagColor;
    private String tagSelectedColor;
    private int typeId;

    public UserTag() {
    }

    private UserTag(Parcel in) {
        this.tagId = in.readInt();
        this.tagName = in.readString();
        this.tagColor = in.readString();
        this.tagSelectedColor = in.readString();
        this.typeId = in.readInt();
    }

    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagColor() {
        return tagColor;
    }

    public void setTagColor(String tagColor) {
        this.tagColor = tagColor;
    }

    public String getTagSelectedColor() {
        return tagSelectedColor;
    }

    public void setTagSelectedColor(String tagSelectedColor) {
        this.tagSelectedColor = tagSelectedColor;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int parseTagColor() {
        return parseColor(this.tagColor, DEFAULT_TAG_COLOR);
    }

    public int parseTagSelectedColor() {
        return parseColor(this.tagSelectedColor, DEFAULT_TAG_SELECTED_COLOR);
    }

    private int parseColor(String colorString, int defaultColor) {
        if (!TextUtils.isEmpty(colorString)) {
            try {
                return ColorUtils.parseColor(colorString);
            } catch (Exception e) {
                L.w(TAG, "parseColor(), error, color=" + colorString);
                return defaultColor;
            }
        } else {
            return defaultColor;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserTag userTag = (UserTag) o;

        return tagId == userTag.tagId;

    }

    @Override
    public int hashCode() {
        return tagId;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.tagId);
        dest.writeString(this.tagName);
        dest.writeString(this.tagColor);

        dest.writeString(this.tagSelectedColor);
        dest.writeInt(this.typeId);
    }

    public static final Parcelable.Creator<UserTag> CREATOR = new Parcelable.Creator<UserTag>() {
        public UserTag createFromParcel(Parcel source) {
            return new UserTag(source);
        }

        public UserTag[] newArray(int size) {
            return new UserTag[size];
        }
    };
}

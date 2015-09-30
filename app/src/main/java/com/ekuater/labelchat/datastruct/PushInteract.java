package com.ekuater.labelchat.datastruct;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.ekuater.labelchat.util.ColorUtils;
import com.ekuater.labelchat.util.L;

/**
 * Created by Administrator on 2015/5/7.
 */
public class PushInteract implements Parcelable{

    public static final String TYPE_INTERACT_TAG  = "1";
    public static final String TYPE_INTERACT_INTEREST = "2";

    private static final int DEFAULT_TAG_COLOR = Color.WHITE;
    private static final int DEFAULT_TAG_SELECTED_COLOR = Color.LTGRAY;

    private String interactType;
    private String objectType;
    private String selectColor;
    private String interactObject;
    private String interactOperate;
    private LiteStranger stranger;

    public PushInteract(){

    }

    public PushInteract(Parcel parcel){
        this.interactType = parcel.readString();
        this.objectType = parcel.readString();
        this.selectColor = parcel.readString();
        this.interactObject = parcel.readString();
        this.interactOperate = parcel.readString();
        this.stranger = parcel.readParcelable(LiteStranger.class.getClassLoader());
    }

    public String getInteractType() {
        return interactType;
    }

    public void setInteractType(String interactType) {
        this.interactType = interactType;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getSelectColor() {
        return selectColor;
    }

    public void setSelectColor(String selectColor) {
        this.selectColor = selectColor;
    }

    public String getInteractObject() {
        return interactObject;
    }

    public void setInteractObject(String interactObject) {
        this.interactObject = interactObject;
    }

    public String getInteractOperate() {
        return interactOperate;
    }

    public void setInteractOperate(String interactOperate) {
        this.interactOperate = interactOperate;
    }

    public LiteStranger getStranger() {
        return stranger;
    }

    public int parseTagColor() {
        return parseColor(this.selectColor, DEFAULT_TAG_COLOR);
    }

    public int parseTagSelectedColor() {
        return parseColor(this.selectColor, DEFAULT_TAG_SELECTED_COLOR);
    }

    private int parseColor(String colorString, int defaultColor) {
        if (!TextUtils.isEmpty(colorString)) {
            try {
                return ColorUtils.parseColor(colorString);
            } catch (Exception e) {
                return defaultColor;
            }
        } else {
            return defaultColor;
        }
    }

    public void setStranger(LiteStranger stranger) {
        this.stranger = stranger;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(interactType);
        dest.writeString(objectType);
        dest.writeString(selectColor);
        dest.writeString(interactObject);
        dest.writeString(interactOperate);
        dest.writeParcelable(stranger, flags);
    }

    public static final Parcelable.Creator<PushInteract> CREATOR = new Parcelable.Creator<PushInteract>(){

        @Override
        public PushInteract createFromParcel(Parcel source) {
            return new PushInteract(source);
        }

        @Override
        public PushInteract[] newArray(int size) {
            return new PushInteract[size];
        }
    };

}

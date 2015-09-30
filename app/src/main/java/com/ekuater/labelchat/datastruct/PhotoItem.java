package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Leo on 2015/1/6.
 *
 * @author LinYong
 */
public class PhotoItem implements Parcelable {

    private String id;
    private String photo;
    private String photoThumb;

    public PhotoItem() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPhotoThumb() {
        return photoThumb;
    }

    public void setPhotoThumb(String photoThumb) {
        this.photoThumb = photoThumb;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getId());
        dest.writeString(getPhoto());
        dest.writeString(getPhotoThumb());
    }

    public static final Parcelable.Creator<PhotoItem> CREATOR
            = new Parcelable.Creator<PhotoItem>() {

        @Override
        public PhotoItem createFromParcel(Parcel source) {
            PhotoItem instance = new PhotoItem();

            instance.setId(source.readString());
            instance.setPhoto(source.readString());
            instance.setPhotoThumb(source.readString());

            return instance;
        }

        @Override
        public PhotoItem[] newArray(int size) {
            return new PhotoItem[size];
        }
    };
}

package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Leo on 2015/3/19.
 *
 * @author LinYong
 */
public class AlbumPhoto implements Parcelable {

    private String photoId;
    private String userId;
    private long createDate;
    private String photo;
    private String photoThumb;
    private boolean isLiked;
    private boolean isSaw;
    private boolean isReminded;
    private int praiseNum;
    private int notifyUploadNum;

    public AlbumPhoto() {
    }

    private AlbumPhoto(Parcel in) {
        this.photoId = in.readString();
        this.userId = in.readString();
        this.createDate = in.readLong();
        this.photo = in.readString();
        this.photoThumb = in.readString();
        this.isLiked = (in.readByte() != 0);
        this.isSaw = (in.readByte() != 0);
        this.isReminded = (in.readByte() != 0);
        this.praiseNum = (in.readInt());
        this.notifyUploadNum = (in.readInt());
    }

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
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

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }

    public boolean isSaw() {
        return isSaw;
    }

    public void setSaw(boolean isRead) {
        this.isSaw = isRead;
    }

    public boolean isReminded() {
        return isReminded;
    }

    public void setReminded(boolean isReminded) {
        this.isReminded = isReminded;
    }

    public int getNotifyUploadNum() {
        return notifyUploadNum;
    }

    public void setNotifyUploadNum(int notifyUploadNum) {
        this.notifyUploadNum = notifyUploadNum;
    }

    public int getPraiseNum() {
        return praiseNum;
    }

    public void setPraiseNum(int praiseNum) {
        this.praiseNum = praiseNum;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.photoId);
        dest.writeString(this.userId);
        dest.writeLong(this.createDate);
        dest.writeString(this.photo);
        dest.writeString(this.photoThumb);
        dest.writeByte((byte) (this.isLiked ? 1 : 0));
        dest.writeByte((byte) (this.isSaw ? 1 : 0));
        dest.writeByte((byte) (this.isReminded ? 1 : 0));
        dest.writeInt(this.praiseNum);
        dest.writeInt(this.notifyUploadNum);
    }

    public static final Parcelable.Creator<AlbumPhoto> CREATOR
            = new Parcelable.Creator<AlbumPhoto>() {
        public AlbumPhoto createFromParcel(Parcel source) {
            return new AlbumPhoto(source);
        }

        public AlbumPhoto[] newArray(int size) {
            return new AlbumPhoto[size];
        }
    };
}

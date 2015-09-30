package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Leo on 2015/1/6.
 *
 * @author LinYong
 */
public class ThrowPhoto implements Parcelable {

    private String id;
    private String userId;
    private long throwDate;
    private LocationInfo location;
    private int pickTotal;
    private String displayPhoto;
    private PhotoItem[] photoArray;
	private PickPhotoUser[] photoChecks;

    public ThrowPhoto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getThrowDate() {
        return throwDate;
    }

    public void setThrowDate(long throwDate) {
        this.throwDate = throwDate;
    }

    public LocationInfo getLocation() {
        return location;
    }

    public void setLocation(LocationInfo location) {
        this.location = location;
    }

    public int getPickTotal() {
        return pickTotal;
    }

    public void setPickTotal(int pickTotal) {
        this.pickTotal = pickTotal;
    }

    public String getDisplayPhoto() {
        return displayPhoto;
    }

    public void setDisplayPhoto(String displayPhoto) {
        this.displayPhoto = displayPhoto;
    }

    public PhotoItem[] getPhotoArray() {
        return photoArray;
    }

    public void setPhotoArray(PhotoItem[] photoArray) {
        this.photoArray = photoArray;
    }

    public PickPhotoUser[] getPhotoChecks() {
        return photoChecks;
    }

    public void setPhotoChecks(PickPhotoUser[] photoChecks) {
        this.photoChecks = photoChecks;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getId());
        dest.writeString(getUserId());
        dest.writeLong(getThrowDate());
        dest.writeParcelable(getLocation(), flags);
        dest.writeInt(getPickTotal());
        dest.writeString(getDisplayPhoto());
        dest.writeParcelableArray(getPhotoArray(), flags);
        dest.writeParcelableArray(getPhotoChecks(), flags);
    }

    public static final Parcelable.Creator<ThrowPhoto> CREATOR
            = new Parcelable.Creator<ThrowPhoto>() {

        @Override
        public ThrowPhoto createFromParcel(Parcel source) {
            ThrowPhoto instance = new ThrowPhoto();

            instance.setId(source.readString());
            instance.setUserId(source.readString());
            instance.setThrowDate(source.readLong());
            instance.setLocation(source.<LocationInfo>readParcelable(
                    LocationInfo.class.getClassLoader()));
            instance.setPickTotal(source.readInt());
            instance.setDisplayPhoto(source.readString());

            final Parcelable[] parcelables = source.readParcelableArray(
                    BaseLabel.class.getClassLoader());
            PhotoItem[] photoArray;
            if (parcelables != null && parcelables.length > 0) {
                photoArray = new PhotoItem[parcelables.length];

                for (int i = 0; i < parcelables.length; ++i) {
                    photoArray[i] = (PhotoItem) parcelables[i];
                }
            } else {
                photoArray = null;
            }
            final Parcelable[] checkPhotoParcelabel = source.readParcelableArray(PickPhotoUser.class.getClassLoader());
            if (checkPhotoParcelabel != null && checkPhotoParcelabel.length > 0) {
                instance.photoChecks = new PickPhotoUser[checkPhotoParcelabel.length];
                for (int i = 0; i < checkPhotoParcelabel.length; ++i) {
                    instance.photoChecks[i] = (PickPhotoUser) checkPhotoParcelabel[i];
                }
            } else {
                instance.photoChecks = null;
            }

            instance.setPhotoArray(photoArray);

            return instance;
        }

        @Override
        public ThrowPhoto[] newArray(int size) {
            return new ThrowPhoto[size];
        }
    };
}

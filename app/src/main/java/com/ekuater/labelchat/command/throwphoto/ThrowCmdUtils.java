package com.ekuater.labelchat.command.throwphoto;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.PhotoItem;
import com.ekuater.labelchat.datastruct.PickPhotoUser;
import com.ekuater.labelchat.datastruct.ThrowPhoto;
import com.ekuater.labelchat.util.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Leo on 2015/1/6.
 *
 * @author LinYong
 */
public final class ThrowCmdUtils {

    private static final String TAG = ThrowCmdUtils.class.getSimpleName();


    public static PhotoItem toPhotoItem(JSONObject json) {
        if (json == null) {
            return null;
        }

        PhotoItem photoItem = null;

        try {
            String id = json.getString(CommandFields.ThrowPhoto.PHOTO_ID);
            String photo = json.getString(CommandFields.ThrowPhoto.PHOTO);
            String photoThumb = json.getString(CommandFields.ThrowPhoto.PHOTO_THUMB);

            photoItem = new PhotoItem();
            photoItem.setId(id);
            photoItem.setPhoto(photo);
            photoItem.setPhotoThumb(photoThumb);
        } catch (JSONException e) {
            L.w(TAG, e);
        }

        return photoItem;
    }

    public static PhotoItem[] toPhotoItemArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }

        ArrayList<PhotoItem> list = new ArrayList<PhotoItem>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                PhotoItem photoItem = toPhotoItem(json);
                if (photoItem != null) {
                    list.add(photoItem);
                }
            }
        }

        final int size = list.size();
        return (size > 0) ? list.toArray(new PhotoItem[size]) : null;
    }

    public static PickPhotoUser[] toPhotoCheckArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }
        ArrayList<PickPhotoUser> list = new ArrayList<PickPhotoUser>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                PickPhotoUser photoCheck = toPhotoCheck(json);
                if (photoCheck != null) {
                    list.add(photoCheck);
                }
            }
        }
        final int size = list.size();
        return (size > 0) ? list.toArray(new PickPhotoUser[size]) : null;
    }

    public static PickPhotoUser toPhotoCheck(JSONObject json) {
        if (json == null) {
            return null;
        }
        PickPhotoUser pickPhotoUser = null;
        try {
            String userId = json.getString(CommandFields.User.USER_ID);
            String userName = json.optString(CommandFields.User.NICKNAME);
            String userAvatarThumb = json.getString(CommandFields.User.AVATAR_THUMB);
            long pickPhotoDate = json.getLong(CommandFields.ThrowPhoto.PICK_PHOTO_DATE);
            pickPhotoUser = new PickPhotoUser();
            pickPhotoUser.setPickUserId(userId);
            pickPhotoUser.setPickUserName(userName);
            pickPhotoUser.setPickUserAvatarThumb(userAvatarThumb);
            pickPhotoUser.setPickPhotoDate(pickPhotoDate);
        } catch (JSONException e) {
            L.w(TAG, e);
        }
        return pickPhotoUser;
    }


    public static ThrowPhoto toThrowPhoto(JSONObject json) {
        if (json == null) {
            return null;
        }

        ThrowPhoto throwPhoto = null;

        try {
            String id = json.getString(CommandFields.ThrowPhoto.THROW_PHOTO_ID);
            String userId = json.getString(CommandFields.ThrowPhoto.USER_ID);
            long throwDate = json.getLong(CommandFields.ThrowPhoto.THROW_DATE);
            double longitude = json.getDouble(CommandFields.ThrowPhoto.LONGITUDE);
            double latitude = json.getDouble(CommandFields.ThrowPhoto.LATITUDE);
            LocationInfo location = new LocationInfo(longitude, latitude);
            int pickTotal = json.getInt(CommandFields.ThrowPhoto.PICK_TOTAL);
            String displayPhoto = json.getString(CommandFields.ThrowPhoto.DISPLAY_PHOTO);
            PhotoItem[] photoArray = toPhotoItemArray(json.getJSONArray(
                    CommandFields.ThrowPhoto.PHOTO_ARRAY));
            PickPhotoUser[] photoChecks = toPhotoCheckArray(json.optJSONArray(CommandFields.ThrowPhoto.PHOTO_CHECK));

            throwPhoto = new ThrowPhoto();
            throwPhoto.setId(id);
            throwPhoto.setUserId(userId);
            throwPhoto.setThrowDate(throwDate);
            throwPhoto.setLocation(location);
            throwPhoto.setPickTotal(pickTotal);
            throwPhoto.setDisplayPhoto(displayPhoto);
            throwPhoto.setPhotoArray(photoArray);
            throwPhoto.setPhotoChecks(photoChecks);
        } catch (JSONException e) {
            L.w(TAG, e);
        }

        return throwPhoto;
    }

    public static ThrowPhoto[] toThrowPhotoArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }

        ArrayList<ThrowPhoto> list = new ArrayList<ThrowPhoto>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                ThrowPhoto throwPhoto = toThrowPhoto(json);
                if (throwPhoto != null) {
                    list.add(throwPhoto);
                }
            }
        }

        final int size = list.size();
        return (size > 0) ? list.toArray(new ThrowPhoto[size]) : null;
    }
}

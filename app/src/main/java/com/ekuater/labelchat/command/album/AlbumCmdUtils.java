package com.ekuater.labelchat.command.album;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.datastruct.AlbumPhoto;
import com.ekuater.labelchat.util.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Leo on 2015/3/19.
 *
 * @author LinYong
 */
public class AlbumCmdUtils {

    private static final String TAG = AlbumCmdUtils.class.getSimpleName();

    public static AlbumPhoto toAlbumPhoto(JSONObject json) {
        if (json == null) {
            return null;
        }

        AlbumPhoto albumPhoto = null;

        try {
            String photoId = json.getString(CommandFields.Album.PHOTO_ID);
            String userId = json.getString(CommandFields.Album.USER_ID);
            long createDate = json.getLong(CommandFields.Album.CREATE_DATE);
            String photo = json.getString(CommandFields.Album.PHOTO);
            String photoThumb = json.getString(CommandFields.Album.PHOTO_THUMB);
            boolean isLiked = CommandFields.Album.YES.equals(json.optString(
                    CommandFields.Album.IS_LIKE));
            boolean isSaw = CommandFields.Album.YES.equals(json.optString(
                    CommandFields.Album.IS_SAW));
            boolean isReminded = CommandFields.Album.YES.equals(json.optString(
                    CommandFields.Album.IS_REMINDED));
            int praiseNum = json.optInt(CommandFields.Album.LIKE_NUM);
            int notifiUploadNum = json.optInt(CommandFields.Album.NOTIFY_UPLOAD_NUM);

            albumPhoto = new AlbumPhoto();
            albumPhoto.setPhotoId(photoId);
            albumPhoto.setUserId(userId);
            albumPhoto.setCreateDate(createDate);
            albumPhoto.setPhoto(photo);
            albumPhoto.setPhotoThumb(photoThumb);
            albumPhoto.setLiked(isLiked);
            albumPhoto.setSaw(isSaw);
            albumPhoto.setReminded(isReminded);
            albumPhoto.setPraiseNum(praiseNum);
            albumPhoto.setNotifyUploadNum(notifiUploadNum);
        } catch (JSONException e) {
            L.w(TAG, e);
        }

        return albumPhoto;
    }

    public static JSONObject toJson(AlbumPhoto albumPhoto) {
        if (albumPhoto == null) {
            return null;
        }

        JSONObject json = null;

        try {
            json = new JSONObject();
            json.put(CommandFields.Album.PHOTO_ID, albumPhoto.getPhotoId());
            json.put(CommandFields.Album.USER_ID, albumPhoto.getUserId());
            json.put(CommandFields.Album.CREATE_DATE, albumPhoto.getCreateDate());
            json.put(CommandFields.Album.PHOTO, albumPhoto.getPhoto());
            json.put(CommandFields.Album.PHOTO_THUMB, albumPhoto.getPhotoThumb());
        } catch (JSONException e) {
            L.w(TAG, e);
        }

        return json;
    }

    public static AlbumPhoto[] toAlbumPhotoArray(JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.length() <= 0) {
            return null;
        }

        ArrayList<AlbumPhoto> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.optJSONObject(i);
            if (json != null) {
                AlbumPhoto albumPhoto = toAlbumPhoto(json);
                if (albumPhoto != null) {
                    list.add(albumPhoto);
                }
            }
        }

        final int size = list.size();
        return (size > 0) ? list.toArray(new AlbumPhoto[size]) : null;
    }

    public static JSONArray toJsonArray(AlbumPhoto[] albumPhotos) {
        JSONArray jsonArray = null;

        if (albumPhotos != null && albumPhotos.length > 0) {
            ArrayList<JSONObject> list = new ArrayList<>();

            for (AlbumPhoto label : albumPhotos) {
                JSONObject json = toJson(label);
                if (json != null) {
                    list.add(json);
                }
            }

            if (list.size() > 0) {
                jsonArray = new JSONArray(list);
            }
        }

        return jsonArray;
    }
}

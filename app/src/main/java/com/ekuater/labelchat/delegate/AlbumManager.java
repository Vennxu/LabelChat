package com.ekuater.labelchat.delegate;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.CommandErrorCode;
import com.ekuater.labelchat.command.album.DeletePhotoCommand;
import com.ekuater.labelchat.command.album.LatestPhotosCommand;
import com.ekuater.labelchat.command.album.LikePhotoCommand;
import com.ekuater.labelchat.command.album.ListPhotosCommand;
import com.ekuater.labelchat.command.album.PhotoLikeUserCommand;
import com.ekuater.labelchat.command.album.PhotoNotifyCommand;
import com.ekuater.labelchat.command.album.UploadPhotoCommand;
import com.ekuater.labelchat.datastruct.AlbumPhoto;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.FollowUser;
import com.ekuater.labelchat.datastruct.LiteStranger;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.delegate.imageloader.DisplayOptions;
import com.ekuater.labelchat.delegate.imageloader.OnlineImageLoader;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Leo on 2015/3/19.
 *
 * @author LinYong
 */
public class AlbumManager extends BaseManager {

    private static final String TAG = AlbumManager.class.getSimpleName();

    public static final int QUERY_RESULT_SUCCESS = 0;
    public static final int QUERY_RESULT_ILLEGAL_ARGUMENTS = 1;
    public static final int QUERY_RESULT_QUERY_FAILURE = 2;
    public static final int QUERY_RESULT_RESPONSE_DATA_ERROR = 3;

    public static final int UPLOAD_SUCCESS = 0;
    public static final int UPLOAD_FAILURE = 1;

    public static final String NOTIFY_TYPE_HAS_SEEN = "1";
    public static final String NOTIFY_TYPE_UPLOAD_MORE = "2";
    public static final String NOTIFY_TYPE_PRAISE = "3";

    public interface PhotoObserver {
        public void onQueryResult(int result, AlbumPhoto[] photos);
    }

    public interface UploadPhotoObserver {
        public void onUploadResult(int result, AlbumPhoto uploadedPhoto);
    }

    public interface LikeUserObserver {
        public void onQueryResult(int result, LiteStranger[] users);
    }

    private static AlbumManager sSingleton;
    private static ContactsManager mContactsManager;
    private static FollowingManager mFollowingManager;

    private static synchronized void initInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new AlbumManager(context.getApplicationContext());
        }
    }

    public static AlbumManager getInstance(Context context) {
        if (sSingleton == null) {
            initInstance(context);
        }
        return sSingleton;
    }

    private final OnlineImageLoader mImageLoader;
    private final String mPhotoBaseUrl;
    private final String mPhotoThumbBaseUrl;

    private AlbumManager(Context context) {
        super(context);
        mImageLoader = OnlineImageLoader.getInstance(context);
        mPhotoBaseUrl = context.getString(R.string.config_album_photo_url);
        mPhotoThumbBaseUrl = context.getString(R.string.config_album_photo_thumb_url);
        mContactsManager = ContactsManager.getInstance(context);
        mFollowingManager = FollowingManager.getInstance(context);
    }

    public void getMyPhotos(PhotoObserver observer) {
        getUserPhotos(null, observer);
    }

    public void getUserPhotos(String queryUserId, PhotoObserver observer) {
        if (observer == null) {
            return;
        }

        ListPhotosCommand command = new ListPhotosCommand(getSession(), getUserId());
        if (!TextUtils.isEmpty(queryUserId)) {
            command.putParamQueryUserId(queryUserId);
        }
        ICommandResponseHandler handler = new ListPhotoQueryHandler(observer);
        executeCommand(command, handler);
    }

    public void getMyLatestPhotos(PhotoObserver observer) {
        getUserLatestPhotos(null, observer);
    }

    public void getUserLatestPhotos(String queryUserId, PhotoObserver observer) {
        if (observer == null) {
            return;
        }

        LatestPhotosCommand command = new LatestPhotosCommand(getSession(), getUserId());
        if (!TextUtils.isEmpty(queryUserId)) {
            command.putParamQueryUserId(queryUserId);
        }
        ICommandResponseHandler handler = new LatestPhotoQueryHandler(observer);
        executeCommand(command, handler);
    }

    public void deletePhoto(String photoId, FunctionCallListener listener) {
        DeletePhotoCommand command = new DeletePhotoCommand(getSession(), getUserId());
        command.putParamPhotoId(photoId);
        ICommandResponseHandler handler = new CommonResponseHandler(listener);
        executeCommand(command, handler);
    }

    public void likePhoto(String photoId, FunctionCallListener listener) {
        LikePhotoCommand command = new LikePhotoCommand(getSession(), getUserId());
        command.putParamPhotoId(photoId);
        ICommandResponseHandler handler = new CommonResponseHandler(listener);
        executeCommand(command, handler);
    }

    public void sendPhotoNotify(AlbumPhoto photo, String notifyType,
                                FunctionCallListener listener) {
        sendPhotoNotify(photo.getPhotoId(), photo.getUserId(), notifyType, listener);
    }

    public void sendPhotoNotify(String photoId, String photoUserId, String notifyType,
                                FunctionCallListener listener) {
        if (TextUtils.isEmpty(photoId) || TextUtils.isEmpty(photoUserId)
                || TextUtils.isEmpty(notifyType)) {
            if (listener != null) {
                listener.onCallResult(FunctionCallListener.RESULT_ILLEGAL_ARGUMENT,
                        CommandErrorCode.EXECUTE_FAILED, null);
            }
            return;
        }

        PhotoNotifyCommand command = new PhotoNotifyCommand(getSession(), getUserId());
        command.putParamPhotoId(photoId);
        command.putParamPhotoUserId(photoUserId);
        command.putParamPhotoNotifyType(notifyType);
        ICommandResponseHandler handler = new CommonResponseHandler(listener);
        executeCommand(command, handler);
    }

    public void getPhotoLikeUsers(String photoId, LikeUserObserver observer) {
        if (observer == null) {
            return;
        }

        PhotoLikeUserCommand command = new PhotoLikeUserCommand(getSession());
        command.putParamPhotoId(photoId);
        ICommandResponseHandler handler = new LikeUserHandler(observer);
        executeCommand(command, handler);
    }

    public void uploadPhotos(File[] photos, String userId, UploadPhotoObserver observer)
            throws FileNotFoundException {
        UploadPhotoCommand command = new UploadPhotoCommand(getSession(), getUserId());

        for (File photo : photos) {
            command.addPhoto(photo);
        }
        command.putParamRelatedUser(userId);
        IUploadResponseHandler handler = new UploadPhotoHandler(observer);
        mCoreService.doUpload(command, handler);
    }

    public void uploadPhoto(File photo, String userId, UploadPhotoObserver observer)
            throws FileNotFoundException {
        uploadPhotos(new File[]{photo}, userId, observer);
    }

    public void displayPhoto(String url, ImageView imageView, int defaultIcon) {
        if (!TextUtils.isEmpty(url)) {
            displayPhotoByUrl(getPhotoUrl(url), imageView, defaultIcon);
        }
    }

    public void displayPhotoThumb(String url, ImageView imageView, int defaultIcon) {
        if (!TextUtils.isEmpty(url)) {
            displayPhotoByUrl(getPhotoThumbUrl(url), imageView, defaultIcon);
        }
    }

    private void displayPhotoByUrl(String fullUrl, ImageView imageView, int defaultIcon) {
        DisplayOptions options = new DisplayOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .defaultImageRes(defaultIcon)
                .build();
        mImageLoader.displayImage(fullUrl, imageView, options);
    }

    private String parseUrl(String url) {
        final int idx = url.lastIndexOf("/");
        return (idx >= 0 && idx < (url.length() - 1)) ? url.substring(idx + 1) : url;
    }

    private String getPhotoUrl(String url) {
        return mPhotoBaseUrl + parseUrl(url);
    }

    private String getPhotoThumbUrl(String url) {
        return mPhotoThumbBaseUrl + parseUrl(url);
    }

    private static class UploadPhotoHandler implements IUploadResponseHandler {

        private final UploadPhotoObserver observer;

        public UploadPhotoHandler(UploadPhotoObserver observer) {
            this.observer = observer;
        }

        @Override
        public void onResponse(int result, String response) {
            int uploadResult = UPLOAD_FAILURE;
            AlbumPhoto uploadedPhoto = null;

            L.v(TAG, "onResponse(), response=" + response);

            if (result == ConstantCode.EXECUTE_RESULT_SUCCESS) {
                try {
                    UploadPhotoCommand.CommandResponse cmdResp
                            = new UploadPhotoCommand.CommandResponse(response);
                    if (cmdResp.requestSuccess()) {
                        uploadResult = UPLOAD_SUCCESS;
                        uploadedPhoto = cmdResp.getUploadedPhoto();
                    }
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
            }
            this.observer.onUploadResult(uploadResult, uploadedPhoto);
        }

        @Override
        public void onProgress(long bytesWritten, long totalSize) {
        }
    }

    private static class ListPhotoQueryHandler extends PhotoQueryHandler {

        private ListPhotosCommand.CommandResponse mCmdResp;

        public ListPhotoQueryHandler(PhotoObserver observer) {
            super(observer);
            mCmdResp = null;
        }

        @Override
        protected void parseResponse(String response) {
            mCmdResp = null;
            try {
                mCmdResp = new ListPhotosCommand.CommandResponse(response);
            } catch (JSONException e) {
                L.w(TAG, e);
            }
        }

        @Override
        protected AlbumPhoto[] getPhotos() {
            return mCmdResp != null ? mCmdResp.getPhotos() : null;
        }
    }

    private static class LatestPhotoQueryHandler extends PhotoQueryHandler {

        private LatestPhotosCommand.CommandResponse mCmdResp;

        public LatestPhotoQueryHandler(PhotoObserver observer) {
            super(observer);
            mCmdResp = null;
        }

        @Override
        protected void parseResponse(String response) {
            mCmdResp = null;
            try {
                mCmdResp = new LatestPhotosCommand.CommandResponse(response);
            } catch (JSONException e) {
                L.w(TAG, e);
            }
        }

        @Override
        protected AlbumPhoto[] getPhotos() {
            return mCmdResp != null ? mCmdResp.getPhotos() : null;
        }
    }

    private static abstract class PhotoQueryHandler implements ICommandResponseHandler {

        private final PhotoObserver observer;

        public PhotoQueryHandler(PhotoObserver observer) {
            this.observer = observer;
        }

        @Override
        public void onResponse(int result, String response) {
            if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null);
                return;
            }

            try {
                BaseCommand.CommandResponse cmdResp = new BaseCommand.CommandResponse(response);
                AlbumPhoto[] photos = null;
                int _ret = QUERY_RESULT_QUERY_FAILURE;

                if (cmdResp.requestSuccess()) {
                    parseResponse(response);
                    photos = getPhotos();
                    _ret = QUERY_RESULT_SUCCESS;
                }

                observer.onQueryResult(_ret, photos);
                return;
            } catch (JSONException e) {
                L.w(TAG, e);
            }
            observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null);
        }

        protected abstract void parseResponse(String response);

        protected abstract AlbumPhoto[] getPhotos();
    }

    private static class LikeUserHandler implements ICommandResponseHandler {

        private final LikeUserObserver observer;

        public LikeUserHandler(LikeUserObserver observer) {
            this.observer = observer;
        }

        @Override
        public void onResponse(int result, String response) {
            if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null);
                return;
            }

            try {
                PhotoLikeUserCommand.CommandResponse cmdResp
                        = new PhotoLikeUserCommand.CommandResponse(response);
                LiteStranger[] users = null;
                int _ret = QUERY_RESULT_QUERY_FAILURE;

                if (cmdResp.requestSuccess()) {
                    users = cmdResp.geLikeUsers();
                    _ret = QUERY_RESULT_SUCCESS;
                }

                observer.onQueryResult(_ret, users);
                return;
            } catch (JSONException e) {
                L.w(TAG, e);
            }
            observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null);
        }
    }

    public String getRelatedUser() {
        ArrayList<String> list = new ArrayList<>();
        UserContact[] userContacts = mContactsManager.getAllUserContact();
        FollowUser[] followUsers = mFollowingManager.getAllFollowingUser();
        if (userContacts != null && userContacts.length > 0) {
            for (UserContact userContact : userContacts) {
                list.add(userContact.getUserId());
            }
        }
        if (followUsers != null && followUsers.length > 0) {
            for (FollowUser followUser : followUsers) {
                list.add(followUser.getUserId());
            }
        }
        HashSet hashSet = new HashSet(list);
        list.clear();
        list.addAll(hashSet);
        StringBuffer sb = new StringBuffer();
        if (list != null && list.size() > 0) {
            for (String str : list) {
                sb.append(str + ";");
            }
        }
        return sb.toString();
    }
}

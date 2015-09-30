package com.ekuater.labelchat.delegate;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.CommandErrorCode;
import com.ekuater.labelchat.command.labels.DelLabelCommand;
import com.ekuater.labelchat.command.labelstory.LabelStoryDeleteCommand;
import com.ekuater.labelchat.command.throwphoto.DeleteThrowCommand;
import com.ekuater.labelchat.command.throwphoto.MapShowCommand;
import com.ekuater.labelchat.command.throwphoto.MyThrowCommand;
import com.ekuater.labelchat.command.throwphoto.PickCommand;
import com.ekuater.labelchat.command.throwphoto.ThrowCommand;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.ThrowPhoto;
import com.ekuater.labelchat.delegate.imageloader.DisplayOptions;
import com.ekuater.labelchat.delegate.imageloader.OnlineImageLoader;
import com.ekuater.labelchat.util.L;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by Leo on 2015/1/5.
 *
 * @author LinYong
 */
public class ThrowPhotoManager extends BaseManager {

    public static enum ResultCode {
        SUCCESS,
        ILLEGAL_ARGUMENTS,
        QUERY_FAILURE,
        RESPONSE_DATA_ERROR,
    }

    public interface ThrowObserver {
        public void onThrowResult(ResultCode result, ThrowPhoto throwPhoto);
    }

    public interface ThrowPhotoQueryObserver {
        public void onQueryResult(ResultCode result, ThrowPhoto[] throwPhotos);
    }

    public interface PickThrowPhotoObserver {
        public void onPickResult(ResultCode result, Stranger userInfo);
    }
    public interface DeleteThrowPhotoObserver {
        public void onDeleteResult(ResultCode result);
    }

    private static final String TAG = ThrowPhotoManager.class.getSimpleName();

    private static final String REAL_URL_PREFIX = "http://";

    private static ThrowPhotoManager sSingleton;

    private static synchronized void initInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new ThrowPhotoManager(context.getApplicationContext());
        }
    }

    public static ThrowPhotoManager getInstance(Context context) {
        if (sSingleton == null) {
            initInstance(context);
        }
        return sSingleton;
    }

    private final Context mContext;
    private final OnlineImageLoader mImageLoader;
    private final String mPhotoBaseUrl;
    private final String mPhotoThumbBaseUrl;

    private ThrowPhotoManager(Context context) {
        super(context);
        mContext = context;
        mImageLoader = OnlineImageLoader.getInstance(context);
        mPhotoBaseUrl = context.getString(R.string.config_throw_photo_url);
        mPhotoThumbBaseUrl = context.getString(R.string.config_throw_photo_thumb_url);
    }

    /**
     * Throw photos
     *
     * @param photos        photo files
     * @param throwPosition throw position
     * @param observer      function call observer
     * @throws FileNotFoundException
     */
    public void throwPhoto(File[] photos, LocationInfo throwPosition,
                           ThrowObserver observer)
            throws FileNotFoundException {
        if (photos == null || photos.length <= 0) {
            throw new NullPointerException("throwPhoto no photos");
        }

        AsyncHttpClient client = newHttpClient();
        ThrowCommand command = new ThrowCommand(getSession(), getUserId());

        command.putParamLocation(throwPosition);
        for (File photo : photos) {
            command.addPhoto(photo);
        }

        client.post(null, getApiRealUrl(command.getUrl()), command.toEntity(), null,
                new ThrowResponseHandler(observer));
    }

    /**
     * Query my throw photos
     *
     * @param observer throw photos query result observer
     */
    public void getMyThrowPhotos(ThrowPhotoQueryObserver observer) {
        getUserThrowPhotos(null, observer);
    }

    /**
     * Query user throw photos
     *
     * @param userId   userId
     * @param observer throw photos query result observer
     */
    public void getUserThrowPhotos(String userId, ThrowPhotoQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        MyThrowCommand command = new MyThrowCommand(getSession(), getUserId());
        if (!TextUtils.isEmpty(userId)) {
            command.putParamQueryUserId(userId);
        }
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                ThrowPhotoQueryObserver observer = (ThrowPhotoQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(ResultCode.QUERY_FAILURE, null);
                    return;
                }

                try {
                    MyThrowCommand.CommandResponse cmdResp
                            = new MyThrowCommand.CommandResponse(response);
                    ThrowPhoto[] throwPhotos = null;
                    ResultCode _ret = ResultCode.QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        throwPhotos = cmdResp.getThrowPhotos();
                        _ret = ResultCode.SUCCESS;
                    }

                    observer.onQueryResult(_ret, throwPhotos);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }

                observer.onQueryResult(ResultCode.RESPONSE_DATA_ERROR, null);
            }
        };
        executeCommand(command, handler);
    }

    /**
     * Query near by throw photos
     *
     * @param observer throw photos query result observer
     */
    public void getNearByThrowPhotos(ThrowPhotoQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        MapShowCommand command = new MapShowCommand(getSession(), getUserId());
        command.putParamLocation(getLocation());
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                ThrowPhotoQueryObserver observer = (ThrowPhotoQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(ResultCode.QUERY_FAILURE, null);
                    return;
                }

                try {
                    MapShowCommand.CommandResponse cmdResp
                            = new MapShowCommand.CommandResponse(response);
                    ThrowPhoto[] throwPhotos = null;
                    ResultCode _ret = ResultCode.QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        throwPhotos = cmdResp.getThrowPhotos();
                        _ret = ResultCode.SUCCESS;
                    }

                    observer.onQueryResult(_ret, throwPhotos);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }

                observer.onQueryResult(ResultCode.RESPONSE_DATA_ERROR, null);
            }
        };
        executeCommand(command, handler);
    }

    /**
     * pick a throw photo
     *
     * @param throwPhotoId throw photo id
     * @param scenario     pick or browse
     * @param observer     pick operation result observer
     */
    public void pickThrowPhoto(String throwPhotoId, String scenario, PickThrowPhotoObserver observer) {
        if (TextUtils.isEmpty(throwPhotoId)) {
            if (observer != null) {
                observer.onPickResult(ResultCode.ILLEGAL_ARGUMENTS, null);
            }
            return;
        }

        PickCommand command = new PickCommand(getSession(), getUserId());
        command.putParamThrowPhotoId(throwPhotoId);
        command.putParamScenario(scenario);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                PickThrowPhotoObserver observer = (PickThrowPhotoObserver) mObj;

                if (observer == null) {
                    return;
                }

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onPickResult(ResultCode.QUERY_FAILURE, null);
                    return;
                }

                try {
                    PickCommand.CommandResponse cmdResp
                            = new PickCommand.CommandResponse(response);
                    Stranger userInfo = null;
                    ResultCode _ret = ResultCode.QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        userInfo = cmdResp.getUserInfo();
                        _ret = ResultCode.SUCCESS;
                    }

                    observer.onPickResult(_ret, userInfo);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }

                observer.onPickResult(ResultCode.RESPONSE_DATA_ERROR, null);
            }
        };
        executeCommand(command, handler);
    }
    /**
     * delete a throw photo
     *
     * @param throwPhotoId throw photo id
     * @param observer     pick operation result observer
     */
    public void deleteThrowPhoto(String throwPhotoId, DeleteThrowPhotoObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        DeleteThrowCommand command = new DeleteThrowCommand(getSession(), getUserId());
        command.putParamThrowPhotoId(throwPhotoId);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {

            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof DeleteThrowPhotoObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                DeleteThrowPhotoObserver observer = (DeleteThrowPhotoObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onDeleteResult(ResultCode.QUERY_FAILURE);
                    return;
                }

                try {
                    LabelStoryDeleteCommand.CommandResponse cmdResp
                            = new LabelStoryDeleteCommand.CommandResponse(response);

                    ResultCode _ret = ResultCode.QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        L.v(TAG, "praiseLabelStory request success");
                        _ret = ResultCode.SUCCESS;
                    }

                    observer.onDeleteResult(_ret);
                    L.v(TAG, "praiseLabelStory _ret=%1$d", _ret);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onDeleteResult(ResultCode.RESPONSE_DATA_ERROR);
            }
        };
        executeCommand(command, handler);
    }
    /**
     * Get photo item bitmap from server
     *
     * @param url      url
     * @param listener asynchronously load listener
     * @return photo Bitmap if cached in memory, or null if not cached in memory
     */
    public Bitmap getPhotoItemBitmap(String url, ShortUrlImageLoadListener listener) {
        if (TextUtils.isEmpty(url)) {
            return null;
        } else {
            return getImageBitmap(getPhotoUrl(url), url, listener);
        }
    }

    /**
     * Get photo item thumb bitmap from server
     *
     * @param url      url
     * @param listener asynchronously load listener
     * @return photo Bitmap if cached in memory, or null if not cached in memory
     */
    public Bitmap getPhotoItemThumbBitmap(String url, ShortUrlImageLoadListener listener) {
        if (TextUtils.isEmpty(url)) {
            return null;
        } else {
            return getImageBitmap(getPhotoThumbUrl(url), url, listener);
        }
    }

    /**
     * Get throw photo display bitmap from server
     *
     * @param url      url
     * @param listener asynchronously load listener
     * @return photo Bitmap if cached in memory, or null if not cached in memory
     */
    public Bitmap getDisplayPhotoBitmap(String url, ShortUrlImageLoadListener listener) {
        return getPhotoItemThumbBitmap(url, listener);
    }

    public void displayPhotoBitmap(String url, ImageView imageView, int defaultIcon) {
        if (!TextUtils.isEmpty(url)) {
            DisplayOptions options = new DisplayOptions.Builder()
                    .cacheInMemory(false)
                    .cacheOnDisk(true)
                    .defaultImageRes(defaultIcon)
                    .build();
            mImageLoader.displayImage(getPhotoThumbUrl(url), imageView, options);
        }
    }

    private AsyncHttpClient newHttpClient() {
        return HttpClient.getHttpClient();
    }

    private String getApiBaseUrl() {
        String baseUrl = mContext.getResources().getString(
                R.string.config_http_api_base_url);

        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        return baseUrl;
    }

    private String getApiRealUrl(String url) {
        String realUrl = url;

        if (!TextUtils.isEmpty(url) && !url.startsWith(REAL_URL_PREFIX)) {
            StringBuilder sb = new StringBuilder();
            sb.append(getApiBaseUrl());
            if (!url.startsWith("/")) {
                sb.append("/");
            }
            sb.append(url);
            realUrl = sb.toString();
        }

        return realUrl;
    }

    private synchronized Bitmap getImageBitmap(String fullUrl, String url,
                                               ShortUrlImageLoadListener listener) {
        ShortUrlImageLoadWrapper wrapper = new ShortUrlImageLoadWrapper(url, listener);
        return mImageLoader.getImageBitmap(fullUrl, wrapper);
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

    private LocationInfo getLocation() {
        return mCoreService.getCurrentLocationInfo();
    }

    private static class ThrowResponseHandler extends JsonHttpResponseHandler {

        private final ThrowObserver mObserver;

        public ThrowResponseHandler(ThrowObserver observer) {
            super();
            mObserver = observer;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            ThrowCommand.CommandResponse cmdResp = new ThrowCommand.CommandResponse(response);
            ResultCode _ret = ResultCode.QUERY_FAILURE;
            ThrowPhoto throwPhoto = null;

            if (cmdResp.requestSuccess()) {
                throwPhoto = cmdResp.getThrowPhoto();
                _ret = ResultCode.SUCCESS;
            }

            L.v(TAG, "onSuccess(), response=" + response.toString());
            notifyResult(_ret, throwPhoto, cmdResp.getErrorCode(), cmdResp.getErrorDesc());
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
            notifyResult(ResultCode.RESPONSE_DATA_ERROR, null, CommandErrorCode.SYSTEM_ERROR, null);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                              JSONObject errorResponse) {
            notifyResult(ResultCode.QUERY_FAILURE, null, CommandErrorCode.SYSTEM_ERROR, null);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                              JSONArray errorResponse) {
            notifyResult(ResultCode.QUERY_FAILURE, null, CommandErrorCode.SYSTEM_ERROR, null);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString,
                              Throwable throwable) {
            notifyResult(ResultCode.QUERY_FAILURE, null, CommandErrorCode.SYSTEM_ERROR, null);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, String responseString) {
            notifyResult(ResultCode.RESPONSE_DATA_ERROR, null, CommandErrorCode.SYSTEM_ERROR, null);
        }

        private void notifyResult(ResultCode result, ThrowPhoto throwPhoto,
                                  int errorCode, String errorDesc) {
            L.v(TAG, "notifyResult(), result=%1$s, errorCode=%2$d, errorDesc=%3$s",
                    result, errorCode, errorDesc);

            if (mObserver != null) {
                mObserver.onThrowResult(result, throwPhoto);
            }
        }
    }
}

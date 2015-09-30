package com.ekuater.labelchat.delegate;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.CommandErrorCode;
import com.ekuater.labelchat.command.labels.HotLabelCommand;
import com.ekuater.labelchat.command.labels.ListLabelCommand;
import com.ekuater.labelchat.command.labels.QueryLabelCommand;
import com.ekuater.labelchat.command.labels.QueryPraiseCommand;
import com.ekuater.labelchat.command.labels.RankLabelCommand;
import com.ekuater.labelchat.command.labels.RecommendLabelCommand;
import com.ekuater.labelchat.command.labels.RecommendStrangerLabelCommand;
import com.ekuater.labelchat.coreservice.command.client.ICommandResponse;
import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LabelPraise;
import com.ekuater.labelchat.datastruct.SystemLabel;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.delegate.imageloader.DisplayOptions;
import com.ekuater.labelchat.delegate.imageloader.OnlineImageLoader;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LinYong
 */
public class UserLabelManager extends BaseManager {

    private static final String TAG = UserLabelManager.class.getSimpleName();

    // Query result enum
    public static final int QUERY_RESULT_SUCCESS = 0;
    public static final int QUERY_RESULT_EMPTY_ARGUMENTS = 1;
    public static final int QUERY_RESULT_ILLEGAL_ARGUMENTS = 2;
    public static final int QUERY_RESULT_QUERY_FAILURE = 3;
    public static final int QUERY_RESULT_RESPONSE_DATA_ERROR = 4;

    public interface IListener extends BaseManager.IListener {

        /**
         * notify the user labels has been updated.
         */
        public void onLabelUpdated();

        /**
         * notify the label add operation result.
         */
        public void onLabelAdded(int result);

        /**
         * notify the label delete operation result.
         */
        public void onLabelDeleted(int result);
    }

    public static class AbsListener implements IListener {

        @Override
        public void onLabelUpdated() {
        }

        @Override
        public void onLabelAdded(int result) {
        }

        @Override
        public void onLabelDeleted(int result) {
        }

        @Override
        public void onCoreServiceConnected() {
        }

        @Override
        public void onCoreServiceDied() {
        }
    }

    public interface ISystemLabelQueryObserver {
        public void onQueryResult(int result, SystemLabel[] labels, boolean remaining);
    }

    public interface RankLabelQueryObserver {
        public void onQueryResult(int result, SystemLabel[] labels, boolean remaining);
    }

    public interface HotLabelQueryObserver {
        public void onQueryResult(int result, SystemLabel[] labels);
    }

    public interface LabelPraiseQueryObserver {
        public void onQueryResult(int result, LabelPraise[] labelPraises);
    }

    private static UserLabelManager sInstance;

    private static synchronized void initInstance(Context context) {
        if (sInstance == null) {
            sInstance = new UserLabelManager(context.getApplicationContext());
        }
    }

    public static UserLabelManager getInstance(Context context) {
        if (sInstance == null) {
            initInstance(context);
        }
        return sInstance;
    }

    private final List<WeakReference<IListener>> mListeners = new ArrayList<WeakReference<IListener>>();
    private final ICoreServiceNotifier mNotifier = new AbstractCoreServiceNotifier() {

        @Override
        public void onCoreServiceConnected() {
            for (int i = mListeners.size() - 1; i >= 0; i--) {
                IListener listener = mListeners.get(i).get();
                if (listener != null) {
                    listener.onCoreServiceConnected();
                } else {
                    mListeners.remove(i);
                }
            }
        }

        @Override
        public void onCoreServiceDied() {
            for (int i = mListeners.size() - 1; i >= 0; i--) {
                IListener listener = mListeners.get(i).get();
                if (listener != null) {
                    listener.onCoreServiceDied();
                } else {
                    mListeners.remove(i);
                }
            }
        }

        @Override
        public void onUserLabelUpdated() {
            for (int i = mListeners.size() - 1; i >= 0; i--) {
                IListener listener = mListeners.get(i).get();
                if (listener != null) {
                    listener.onLabelUpdated();
                } else {
                    mListeners.remove(i);
                }
            }
        }

        @Override
        public void onUserLabelAdded(int result) {
            for (int i = mListeners.size() - 1; i >= 0; i--) {
                IListener listener = mListeners.get(i).get();
                if (listener != null) {
                    listener.onLabelAdded(result);
                } else {
                    mListeners.remove(i);
                }
            }
        }

        @Override
        public void onUserLabelDeleted(int result) {
            for (int i = mListeners.size() - 1; i >= 0; i--) {
                IListener listener = mListeners.get(i).get();
                if (listener != null) {
                    listener.onLabelDeleted(result);
                } else {
                    mListeners.remove(i);
                }
            }
        }
    };

    private final String mLabelImageUrl;
    private final OnlineImageLoader mImageLoader;

    private UserLabelManager(Context context) {
        super(context);
        mCoreService.registerNotifier(mNotifier);
        mLabelImageUrl = context.getString(R.string.config_label_image_url);
        mImageLoader = OnlineImageLoader.getInstance(context);
    }

    public void registerListener(IListener listener) {
        synchronized (mListeners) {
            for (WeakReference<IListener> ref : mListeners) {
                if (ref.get() == listener) {
                    return;
                }
            }

            mListeners.add(new WeakReference<IListener>(listener));
            unregisterListener(null);
        }
    }

    public void unregisterListener(IListener listener) {
        synchronized (mListeners) {
            for (int i = mListeners.size() - 1; i >= 0; i--) {
                if (mListeners.get(i).get() == listener) {
                    mListeners.remove(i);
                }
            }
        }
    }

    public void addUserLabels(BaseLabel[] labels) {
        if (isInGuestMode()) {
            mGuestMode.addUserLabels(labels);
            mNotifier.onUserLabelAdded(ConstantCode.LABEL_OPERATION_SUCCESS);
            mNotifier.onUserLabelUpdated();
            return;
        }

        mCoreService.labelAddUserLabels(labels);
    }

    public void deleteUserLabels(UserLabel[] labels) {
        if (isInGuestMode()) {
            mGuestMode.deleteUserLabels(labels);
            mNotifier.onUserLabelDeleted(ConstantCode.LABEL_OPERATION_SUCCESS);
            return;
        }

        mCoreService.labelDeleteUserLabels(labels);
    }

    public UserLabel[] getAllLabels() {
        if (isInGuestMode()) {
            return mGuestMode.getUserLabels();
        }

        return mCoreService.labelGetAllUserLabels();
    }

    /**
     * Force get user labels from server
     */
    public void forceRefreshLabels() {
        if (isInGuestMode()) {
            return;
        }
        mCoreService.labelForceRefreshUserLabels();
    }

    public void querySystemLabels(String keyword, ISystemLabelQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        if (TextUtils.isEmpty(keyword)) {
            observer.onQueryResult(QUERY_RESULT_EMPTY_ARGUMENTS, null, false);
            return;
        }

        QueryLabelCommand command = new QueryLabelCommand(getSession());
        command.putParamKeyword(keyword);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {

            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof ISystemLabelQueryObserver)) {
                    // no available observer, so do not need to care about the response result.
                    return;
                }

                ISystemLabelQueryObserver observer = (ISystemLabelQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null, false);
                    return;
                }

                try {
                    QueryLabelCommand.CommandResponse cmdResp
                            = new QueryLabelCommand.CommandResponse(response);
                    SystemLabel[] labels = null;
                    int _ret = QUERY_RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        labels = cmdResp.getSysLabels();
                        _ret = QUERY_RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, labels, false);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null, false);
            }
        };
        executeCommand(command, handler);
    }

    /**
     * List all labels on server
     *
     * @param requestTime request time, start from 1
     * @param observer    observer of list result
     */
    public void listSystemLabels(int requestTime, ISystemLabelQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        if (requestTime < 1) {
            observer.onQueryResult(QUERY_RESULT_ILLEGAL_ARGUMENTS, null, false);
            return;
        }

        ListLabelCommand command = new ListLabelCommand(getSession());
        command.putParamRequestTime(requestTime);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof ISystemLabelQueryObserver)) {
                    // no available observer, so do not need to care about the response result.
                    return;
                }

                ISystemLabelQueryObserver observer = (ISystemLabelQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null, false);
                    return;
                }

                try {
                    ListLabelCommand.CommandResponse cmdResp
                            = new ListLabelCommand.CommandResponse(response);
                    SystemLabel[] labels = null;
                    boolean remaining = false;
                    int _ret = QUERY_RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        labels = cmdResp.getSystemLabels();
                        remaining = (labels != null) && (labels.length >= 20);
                        _ret = QUERY_RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, labels, remaining);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null, false);
            }
        };
        executeCommand(command, handler);
    }

    /**
     * Query label praise count of stranger or friend
     *
     * @param queryUserId stranger or friend userId, or query oneself label praise if null.
     * @param observer    label praise count query result observer
     */
    public void queryLabelPraise(String queryUserId, LabelPraiseQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        QueryPraiseCommand command = new QueryPraiseCommand(getSession(), getUserId());
        command.putParamQueryUserId(queryUserId);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                if (!(mObj instanceof LabelPraiseQueryObserver)) {
                    // no available observer, so do not need to care about the response result.
                    return;
                }

                LabelPraiseQueryObserver observer = (LabelPraiseQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null);
                    return;
                }

                try {
                    QueryPraiseCommand.CommandResponse cmdResp
                            = new QueryPraiseCommand.CommandResponse(response);
                    int _ret = QUERY_RESULT_QUERY_FAILURE;
                    LabelPraise[] labelPraises = null;

                    if (cmdResp.requestSuccess()) {
                        labelPraises = cmdResp.getLabelPraises();
                        _ret = QUERY_RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, labelPraises);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null);
            }
        };
        executeCommand(command, handler);
    }

    public void queryHotLabel(HotLabelQueryObserver observer) {
        if (observer == null) {
            return;
        }
        HotLabelCommand command = new HotLabelCommand(getSession(), getUserId());
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof HotLabelQueryObserver)) {
                    return;
                }
                HotLabelQueryObserver observer = (HotLabelQueryObserver) mObj;
                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null);
                    return;
                }
                try {
                    HotLabelCommand.CommandResponse cmdResp = new HotLabelCommand.CommandResponse(response);
                    int _ret = QUERY_RESULT_QUERY_FAILURE;
                    SystemLabel[] systemLabels = null;
                    if (cmdResp.requestSuccess()) {
                        systemLabels = cmdResp.getSystemLabels();
                        _ret = QUERY_RESULT_SUCCESS;
                    }
                    observer.onQueryResult(_ret, systemLabels);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null);
            }
        };
        executeCommand(command, handler);
    }

    public void queryRankLabel(int requestTime, RankLabelQueryObserver observer) {
        if (observer == null) {
            return;
        }
        if (requestTime < 1) {
            observer.onQueryResult(QUERY_RESULT_ILLEGAL_ARGUMENTS, null, false);
            return;
        }
        RankLabelCommand command = new RankLabelCommand(getSession());
        command.putParamRequestTime(requestTime);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof RankLabelQueryObserver)) {
                    return;
                }
                RankLabelQueryObserver observer = (RankLabelQueryObserver) mObj;
                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null, false);
                    return;
                }
                try {
                    RankLabelCommand.CommandResponse cmdResponse = new RankLabelCommand.CommandResponse(response);
                    SystemLabel[] labels = null;
                    boolean remaining = false;
                    int _ret = QUERY_RESULT_QUERY_FAILURE;
                    if (cmdResponse.requestSuccess()) {
                        labels = cmdResponse.getSystemLabels();
                        remaining = (labels != null) && (labels.length >= 20);
                        _ret = QUERY_RESULT_SUCCESS;
                    }
                    observer.onQueryResult(_ret, labels, remaining);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null, false);
            }
        };
        executeCommand(command, handler);
    }

    /**
     * Recommend label to friend
     *
     * @param friendUserId friend user id
     * @param labels       recommended labels
     * @param listener     result listener
     */
    public void recommendLabel(String friendUserId, BaseLabel[] labels,
                               FunctionCallListener listener) {
        if (isInGuestMode()) {
            if (listener != null) {
                listener.onCallResult(FunctionCallListener.RESULT_CALL_SUCCESS,
                        CommandErrorCode.REQUEST_SUCCESS, null);
            }
            return;
        }

        RecommendLabelCommand command = new RecommendLabelCommand(getSession(), getUserId());
        command.putParamFriendUserId(friendUserId);
        command.putParamLabels(labels);
        ICommandResponseHandler handler = new CommonResponseHandler(listener);
        executeCommand(command, handler);
    }

    /**
     * Recommend label to stranger
     *
     * @param strangerUserId friend user id
     * @param labels         recommended labels
     * @param listener       result listener
     */
    public void recommendStrangerLabel(String strangerUserId, BaseLabel[] labels,
                                       FunctionCallListener listener) {
        if (isInGuestMode()) {
            if (listener != null) {
                listener.onCallResult(FunctionCallListener.RESULT_CALL_SUCCESS,
                        CommandErrorCode.REQUEST_SUCCESS, null);
            }
            return;
        }

        RecommendStrangerLabelCommand command = new RecommendStrangerLabelCommand(
                getSession(), getUserId());
        command.putParamStrangerUserId(strangerUserId);
        command.putParamLabels(labels);
        ICommandResponseHandler handler = new CommonResponseHandler(listener);
        executeCommand(command, handler);
    }


    public void displayLabelImage(String image, ImageView imageView, int defaultIcon) {
        if (!TextUtils.isEmpty(image)) {
            mImageLoader.displayImage(getLabelImageUrl(image), imageView,
                    newLabelImageDisplayOptions(defaultIcon));
        }
    }

    private DisplayOptions newLabelImageDisplayOptions(int defaultIcon) {
        return new DisplayOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .defaultImageRes(defaultIcon)
                .build();
    }

    private String getLabelImageUrl(String image) {
        return mLabelImageUrl + parseUrl(image);
    }

    private String parseUrl(String url) {
        final int idx = url.lastIndexOf("/");
        return (idx >= 0 && idx < (url.length() - 1)) ? url.substring(idx + 1) : url;
    }
}

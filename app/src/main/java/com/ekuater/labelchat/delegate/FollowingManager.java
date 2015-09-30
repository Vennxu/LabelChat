package com.ekuater.labelchat.delegate;

import android.content.Context;
import android.text.TextUtils;

import com.ekuater.labelchat.command.CommandErrorCode;
import com.ekuater.labelchat.command.following.FollowerCountCommand;
import com.ekuater.labelchat.command.following.FollowingCancelCommand;
import com.ekuater.labelchat.command.following.FollowingCommand;
import com.ekuater.labelchat.command.following.FollowingCountCommand;
import com.ekuater.labelchat.command.following.InviteNotifyCommand;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.FollowUser;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Label on 2015/3/12.
 *
 * @author XuWenxiang
 */
public class FollowingManager extends BaseManager {

    private static final String TAG = FollowingManager.class.getSimpleName();

    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_ILLEGAL_ARGUMENTS = 1;
    public static final int RESULT_QUERY_FAILURE = 2;
    public static final int RESULT_RESPONSE_DATA_ERROR = 3;

    public interface IListener extends BaseManager.IListener {

        public void onFollowUserDataChanged();
    }

    public static class AbsListener implements IListener {

        @Override
        public void onFollowUserDataChanged() {
        }

        @Override
        public void onCoreServiceConnected() {
        }

        @Override
        public void onCoreServiceDied() {
        }
    }

    public interface FollowingQueryObserver {
        public void onQueryResult(int result, boolean remaining);
    }

    public interface FollowingCountQueryObserver {
        public void onQueryResult(int result, int  followingCount, boolean remaining);
    }

    private interface ListenerNotifier {
        public void notify(IListener listener);
    }

    private static FollowingManager sInstance;

    private static synchronized void initInstance(Context context) {
        if (sInstance == null) {
            sInstance = new FollowingManager(context.getApplicationContext());
        }
    }

    public static FollowingManager getInstance(Context context) {
        if (sInstance == null) {
            initInstance(context);
        }
        return sInstance;
    }

    private final List<WeakReference<IListener>> mListeners = new ArrayList<>();
    private final ICoreServiceNotifier mNotifier = new AbstractCoreServiceNotifier() {
        @Override
        public void onCoreServiceConnected() {
            notifyListeners(new ListenerNotifier() {
                @Override
                public void notify(IListener listener) {
                    listener.onCoreServiceConnected();
                }
            });
        }

        @Override
        public void onCoreServiceDied() {
            notifyListeners(new ListenerNotifier() {
                @Override
                public void notify(IListener listener) {
                    listener.onCoreServiceDied();
                }
            });
        }

        @Override
        public void onFollowUserDataChanged() {
            notifyListeners(new ListenerNotifier() {
                @Override
                public void notify(IListener listener) {
                    listener.onFollowUserDataChanged();
                }
            });
        }
    };

    private FollowingManager(Context context) {
        super(context);
        mCoreService.registerNotifier(mNotifier);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mCoreService.unregisterNotifier(mNotifier);
    }

    public void registerListener(IListener listener) {
        synchronized (mListeners) {
            for (WeakReference<IListener> ref : mListeners) {
                if (ref.get() == listener) {
                    return;
                }
            }

            mListeners.add(new WeakReference<>(listener));
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

    public void followingUserInfo(String followingUserId, FollowingQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        FollowingCommand command = new FollowingCommand(getSession(), getUserId());
        command.putParamFollowId(followingUserId);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {

            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof FollowingQueryObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                FollowingQueryObserver observer = (FollowingQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(RESULT_QUERY_FAILURE, false);
                    return;
                }

                try {
                    FollowingCommand.CommandResponse cmdResp
                            = new FollowingCommand.CommandResponse(response);

                    int _ret = RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        FollowUser followingUser = cmdResp.getFollowingUser();
                        if (followingUser != null) {
                            addFollowingUser(followingUser);
                        }
                        _ret = RESULT_SUCCESS;
                    }
                    observer.onQueryResult(_ret, false);
                    L.v(TAG, "followingUserInfo _ret=%1$d", _ret);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(RESULT_RESPONSE_DATA_ERROR, false);
            }
        };
        executeCommand(command, handler);
    }

    public void followingCancelUserInfo(final String followingUserId, FollowingQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        FollowingCancelCommand command = new FollowingCancelCommand(getSession(), getUserId());
        command.putParamFollowId(followingUserId);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {

            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof FollowingQueryObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                FollowingQueryObserver observer = (FollowingQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(RESULT_QUERY_FAILURE, false);
                    return;
                }

                try {
                    FollowingCancelCommand.CommandResponse cmdResp
                            = new FollowingCancelCommand.CommandResponse(response);

                    int _ret = RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        deleteFollowingUser(followingUserId);
                        _ret = RESULT_SUCCESS;
                    }
                    observer.onQueryResult(_ret, false);
                    L.v(TAG, "followingCancelUserInfo _ret=%1$d", _ret);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(RESULT_RESPONSE_DATA_ERROR, false);
            }
        };
        executeCommand(command, handler);
    }

    public void followingUserCountInfo(String followingUserId, FollowingCountQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        FollowingCommand command = new FollowingCommand(getSession(), getUserId());
        command.putParamFollowId(followingUserId);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {

            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof FollowingCountQueryObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                FollowingCountQueryObserver observer = (FollowingCountQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(RESULT_QUERY_FAILURE, 0, false);
                    return;
                }

                try {
                    FollowingCommand.CommandResponse cmdResp
                            = new FollowingCommand.CommandResponse(response);

                    int _ret = RESULT_QUERY_FAILURE;
                    int followCount = 0;

                    if (cmdResp.requestSuccess()) {
                        FollowUser followingUser = cmdResp.getFollowingUser();
                        if (followingUser != null) {
                            addFollowingUser(followingUser);
                        }
                        _ret = RESULT_SUCCESS;
                        followCount = cmdResp.getFollowerCount();
                    }
                    observer.onQueryResult(_ret, followCount, false);
                    L.v(TAG, "followingUserInfo _ret=%1$d", _ret);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(RESULT_RESPONSE_DATA_ERROR,0,false);
            }
        };
        executeCommand(command, handler);
    }

    public void getFollowingUserCount(FollowingCountQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        FollowingCountCommand command = new FollowingCountCommand(getSession(), getUserId());
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {

            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof FollowingCountQueryObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                FollowingCountQueryObserver observer = (FollowingCountQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(RESULT_QUERY_FAILURE, 0, false);
                    return;
                }

                try {
                    FollowingCountCommand.CommandResponse cmdResp
                            = new FollowingCountCommand.CommandResponse(response);

                    int _ret = RESULT_QUERY_FAILURE;
                    int followingCount = 0;
                    if (cmdResp.requestSuccess()) {
                        followingCount = cmdResp.followingCount();
                        _ret = RESULT_SUCCESS;
                    }
                    observer.onQueryResult(_ret, followingCount, false);
                    L.v(TAG, "getFollowingUserCount _ret=%1$d", _ret);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(RESULT_RESPONSE_DATA_ERROR, 0, false);
            }
        };
        executeCommand(command, handler);
    }

    public void getFollowerUserCount(FollowingCountQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        FollowerCountCommand command = new FollowerCountCommand(getSession(), getUserId());
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {

            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof FollowingCountQueryObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                FollowingCountQueryObserver observer = (FollowingCountQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(RESULT_QUERY_FAILURE, 0, false);
                    return;
                }

                try {
                    FollowerCountCommand.CommandResponse cmdResp
                            = new FollowerCountCommand.CommandResponse(response);

                    int _ret = RESULT_QUERY_FAILURE;
                    int followerCount = 0;
                    if (cmdResp.requestSuccess()) {
                        followerCount = cmdResp.followerCount();
                        _ret = RESULT_SUCCESS;
                    }
                    observer.onQueryResult(_ret, followerCount, false);
                    L.v(TAG, "getFollowerUserCount _ret=%1$d", _ret);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(RESULT_RESPONSE_DATA_ERROR, 0, false);
            }
        };
        executeCommand(command, handler);
    }

    public void sendInviteNotify(String userId, FunctionCallListener listener) {
        if (TextUtils.isEmpty(userId)) {
            if (listener != null) {
                listener.onCallResult(FunctionCallListener.RESULT_ILLEGAL_ARGUMENT,
                        CommandErrorCode.EXECUTE_FAILED, null);
            }
            return;
        }
        InviteNotifyCommand cmd = new InviteNotifyCommand(getSession(), getUserId());
        cmd.putParamUserId(userId);
        ICommandResponseHandler handler = new CommonResponseHandler(listener);
        executeCommand(cmd, handler);
    }

    public FollowUser getFollowingUser(String userId) {
        return mCoreService.getFollowingUser(userId);
    }

    public FollowUser[] batchQueryFollowerUser(String[] userIds) {
        return mCoreService.batchQueryFollowerUser(userIds);
    }

    public FollowUser[] getAllFollowingUser() {
        return mCoreService.getAllFollowingUser();
    }

    public void addFollowingUser(FollowUser followUser) {
        mCoreService.addFollowingUser(followUser);
    }

    public void deleteFollowingUser(String userId) {
        mCoreService.deleteFollowingUser(userId);
    }

    public FollowUser getFollowerUser(String userId) {
        return mCoreService.getFollowerUser(userId);
    }

    public FollowUser[] getAllFollowerUser() {
        return mCoreService.getAllFollowerUser();
    }

    public void addFollowerUser(FollowUser followUser) {
        mCoreService.addFollowerUser(followUser);
    }

    public void deleteFollowerUser(String userId) {
        mCoreService.deleteFollowerUser(userId);
    }

    private void notifyListeners(ListenerNotifier notifier) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            IListener listener = mListeners.get(i).get();
            if (listener != null) {
                notifier.notify(listener);
            } else {
                mListeners.remove(i);
            }
        }
    }
}

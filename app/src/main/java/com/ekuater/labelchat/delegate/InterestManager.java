package com.ekuater.labelchat.delegate;

import android.content.Context;
import android.text.TextUtils;

import com.ekuater.labelchat.command.interest.GetInterestCommand;
import com.ekuater.labelchat.command.interest.PushInteractCommand;
import com.ekuater.labelchat.command.interest.SetInterestCommand;
import com.ekuater.labelchat.command.recent.RecentCommand;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.InterestType;
import com.ekuater.labelchat.datastruct.PushInteract;
import com.ekuater.labelchat.datastruct.RecentVisitor;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/3/21.
 *
 * @author FanChong
 */
public class InterestManager extends BaseManager {
    private static final String TAG = InterestManager.class.getSimpleName();
    public static final int QUERY_RESULT_SUCCESS = 0;
    public static final int QUERY_RESULT_ILLEGAL_ARGUMENTS = 1;
    public static final int QUERY_RESULT_QUERY_FAILURE = 2;
    public static final int QUERY_RESULT_RESPONSE_DATA_ERROR = 3;

    public interface IListener extends BaseManager.IListener {
        public void onUserInterestUpdate();

        public void onAddInterestResult(int result);
    }

    public interface ListenerNotifier {
        public void notify(IListener listener);
    }

    private static InterestManager sInstance;

    private static synchronized void initInstance(Context context) {
        if (sInstance == null) {
            sInstance = new InterestManager(context.getApplicationContext());
        }
    }

    public static InterestManager getInstance(Context context) {
        if (sInstance == null) {
            initInstance(context);
        }
        return sInstance;
    }

    public InterestManager(Context context) {
        super(context);
        mCoreService.registerNotifier(mNotifier);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mCoreService.unregisterNotifier(mNotifier);
    }

    public void registerLisener(IListener listener) {
        synchronized (mListeners) {
            for (WeakReference<IListener> wr : mListeners) {
                if (wr.get() == listener) {
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

    private static final List<WeakReference<IListener>> mListeners = new ArrayList<>();
    private ICoreServiceNotifier mNotifier = new AbstractCoreServiceNotifier() {
    };

    public interface UserInterestObserver {
        public void onQueryResult(int result, InterestType[] interestTypes);
    }

    public interface RecentVisitorObserver {
        public void onQueryResult(int result, RecentVisitor[] recentVisitorss);
    }

    public interface PushInteractObserver {
        public void onQueryResult(int result);
    }

    public void getUserInterest(String queryUserId, UserInterestObserver observer) {
        if (observer == null) {
            return;
        }
        GetInterestCommand command = new GetInterestCommand(getSession(), getUserId());
        command.putParamQueryUserId(queryUserId);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                if (!(mObj instanceof UserInterestObserver)) {
                    return;
                }
                UserInterestObserver observer = (UserInterestObserver) mObj;
                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null);
                    return;
                }
                try {
                    GetInterestCommand.CommandResponse cmdResp = new GetInterestCommand.CommandResponse(response);
                    InterestType[] interestTypes = null;
                    int ret_ = QUERY_RESULT_QUERY_FAILURE;
                    if (cmdResp.requestSuccess()) {
                        interestTypes = cmdResp.getInterests();
                        ret_ = QUERY_RESULT_SUCCESS;
                    }
                    observer.onQueryResult(ret_, interestTypes);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR,null);
            }
        };
        executeCommand(command,handler);
    }
    public void setUserInterest(InterestType interestType, List<String> userIds, String interestName) {
        SetInterestCommand command = new SetInterestCommand(getSession(), getUserId());
        command.putParamInterestTypeId(interestType.getTypeId());
        command.putParamArrayUserId(userIds);
        command.putParamInterestName(interestName);
        if (interestType.getUserInterests() != null && interestType.getUserInterests().length > 0) {
            command.putParamInterestNameArray(interestType.getUserInterests());
        }
        executeCommand(command,null);
    }

    public void getRecentVisitor(String queryUserId, RecentVisitorObserver observer) {
        if (observer == null) {
            return;
        }
        RecentCommand command = new RecentCommand(getSession(), getUserId());
        if (TextUtils.isEmpty(queryUserId)) {
            command.putParamQueryUserId(queryUserId);
        }
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                if (!(mObj instanceof RecentVisitorObserver)) {
                    return;
                }
                RecentVisitorObserver observer = (RecentVisitorObserver) mObj;
                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null);
                    return;
                }
                try {
                    RecentCommand.CommandResponse cmdResp = new RecentCommand.CommandResponse(response);
                    RecentVisitor[] recentVisitors = null;
                    int ret_ = QUERY_RESULT_QUERY_FAILURE;
                    if (cmdResp.requestSuccess()) {
                        recentVisitors = cmdResp.getRecentVisitor();
                        ret_ = QUERY_RESULT_SUCCESS;
                    }
                    observer.onQueryResult(ret_, recentVisitors);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR,null);
            }
        };
        executeCommand(command,handler);
    }

    public void pushInteract(String interactUserId, PushInteract interact, PushInteractObserver observer) {
        if (observer == null) {
            return;
        }
        PushInteractCommand command = new PushInteractCommand(getSession(), getUserId());
        command.putParamInteractUserId(interactUserId);
        command.putParamInteractType(interact.getInteractType());
        command.putParamInteractObject(interact.getInteractObject());
        command.putParamInteractOperate(interact.getInteractOperate());
        command.putParamObjectType(interact.getObjectType());
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                if (!(mObj instanceof PushInteractObserver)) {
                    return;
                }
                PushInteractObserver observer = (PushInteractObserver) mObj;
                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE);
                    return;
                }
                try {
                    PushInteractCommand.CommandResponse cmdResp = new PushInteractCommand.CommandResponse(response);
                    int ret_ = QUERY_RESULT_QUERY_FAILURE;
                    if (cmdResp.requestSuccess()) {
                        ret_ = QUERY_RESULT_SUCCESS;
                    }
                    observer.onQueryResult(ret_);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR);
            }
        };
        executeCommand(command,handler);
    }
}

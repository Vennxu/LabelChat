package com.ekuater.labelchat.coreservice.pushmediator;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.account.PullSystemPushCommand;
import com.ekuater.labelchat.coreservice.ICoreServiceCallback;
import com.ekuater.labelchat.coreservice.command.ICommandResponseHandler;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.RequestCommand;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.settings.Settings;
import com.ekuater.labelchat.util.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LinYong
 */
public class SystemPushPullPool {

    private static final String TAG = SystemPushPullPool.class.getSimpleName();

    public interface IListener {
        public void onNewSystemPush(SystemPush systemPush);
    }

    private interface IListenerNotifier {

        public void notify(IListener listener);
    }

    private static final String UNPROCESSED_NOTICE_KEY = "system_push_unprocessed_notice";

    private static final int MSG_PULL_ALL_SYSTEM_PUSH = 101;
    private static final int MSG_HANDLE_PULL_RESULT = 102;
    private static final int MSG_HANDLE_ADD_NEW_NOTICE = 103;
    private static final int MSG_HANDLE_LOGIN = 104;
    private static final int MSG_HANDLE_LOGOUT = 105;

    private final List<SystemPushNotice> mNoticeQueue;
    private final List<SystemPushNotice> mPullingQueue;
    private final ContentResolver mCR;
    private final ICoreServiceCallback mCallback;
    private final Handler mProcessHandler;
    private final IListener mListener;

    private class ProcessHandler extends Handler {

        public ProcessHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PULL_ALL_SYSTEM_PUSH:
                    handlePullAllSystemPush();
                    break;
                case MSG_HANDLE_PULL_RESULT:
                    handlePullResult(msg.obj);
                    break;
                case MSG_HANDLE_ADD_NEW_NOTICE:
                    handleAddNotice(msg.obj);
                    break;
                case MSG_HANDLE_LOGIN:
                    handleLogin();
                    break;
                case MSG_HANDLE_LOGOUT:
                    handleLogout();
                    break;
                default:
                    break;
            }
        }
    }

    public SystemPushPullPool(Context context, ICoreServiceCallback callback,
                              Looper looper, IListener listener) {
        mNoticeQueue = new ArrayList<SystemPushNotice>();
        mPullingQueue = new ArrayList<SystemPushNotice>();
        mCR = context.getContentResolver();
        mCallback = callback;
        mProcessHandler = new ProcessHandler(looper);
        mListener = listener;
    }

    public void connect() {
        Message message = mProcessHandler.obtainMessage(MSG_HANDLE_LOGIN);
        mProcessHandler.sendMessage(message);
    }

    private void handleLogin() {
        mNoticeQueue.clear();
        mPullingQueue.clear();
        loadUnprocessedNotice();
        pullAllSystemPush();
    }

    public void disconnect() {
        Message message = mProcessHandler.obtainMessage(MSG_HANDLE_LOGOUT);
        mProcessHandler.sendMessage(message);
    }

    private void handleLogout() {
        saveUnprocessedNotice();
        mNoticeQueue.clear();
        mPullingQueue.clear();
    }

    private void loadUnprocessedNotice() {
        String value = Settings.Personal.getString(mCR, UNPROCESSED_NOTICE_KEY);
        L.v(TAG, "loadUnprocessedNotice(), value=%1$s", value);
        if (TextUtils.isEmpty(value)) {
            return;
        }

        try {
            JSONArray jsonArray = new JSONArray(value);
            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.optJSONObject(i);
                    if (json != null) {
                        SystemPushNotice pushNotice = SystemPushNotice.build(json);
                        if (pushNotice != null) {
                            mNoticeQueue.add(pushNotice);
                            L.v(TAG, "loadUnprocessedNotice(), add notice to queue:%1$s",
                                    pushNotice.toString());
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveUnprocessedNotice() {
        JSONArray jsonArray = new JSONArray();

        for (SystemPushNotice notice : mNoticeQueue) {
            JSONObject json = notice.toJson();
            if (json != null) {
                jsonArray.put(notice.toJson());
            }
        }

        Settings.Personal.putString(mCR, UNPROCESSED_NOTICE_KEY, jsonArray.toString());
    }

    public void addNewNotice(SystemPushNotice notice) {
        Message message = mProcessHandler.obtainMessage(MSG_HANDLE_ADD_NEW_NOTICE, notice);
        mProcessHandler.sendMessage(message);
    }

    private void handleAddNotice(Object object) {
        L.v(TAG, "handleAddNotice(), object=%1$s", object.toString());
        if (object instanceof SystemPushNotice) {
            SystemPushNotice notice = (SystemPushNotice) object;
            mNoticeQueue.add(notice);
            saveUnprocessedNotice();
            pullAllSystemPush();
        }
    }

    private void deleteNotice(SystemPushNotice notice) {
        if (notice != null && mNoticeQueue.remove(notice)) {
            saveUnprocessedNotice();
        }
    }

    private void executeCommand(BaseCommand command, ICommandResponseHandler handler) {
        mCallback.executeCommand(command.toRequestCommand(), handler);
    }

    private BaseCommand preTreatCommand(BaseCommand command) {
        return mCallback.preTreatCommand(command);
    }

    private void pullSystemPush(SystemPushNotice notice) {
        L.v(TAG, "pullSystemPush()");
        if (addToPullingQueue(notice)) {
            L.d(TAG, "pullSystemPush(), notice=%1$s", notice.toString());
            PullSystemPushCommand command = new PullSystemPushCommand();
            preTreatCommand(command);
            // command.putParamType(notice.getType());
            command.putParamPushId(notice.getPushId());
            executeCommand(command, new PullSystemPushHandler(notice));
        }
    }

    private void pullAllSystemPush() {
        mProcessHandler.removeMessages(MSG_PULL_ALL_SYSTEM_PUSH);
        Message message = mProcessHandler.obtainMessage(MSG_PULL_ALL_SYSTEM_PUSH);
        mProcessHandler.sendMessage(message);
    }

    private void handlePullAllSystemPush() {
        for (SystemPushNotice notice : mNoticeQueue) {
            pullSystemPush(notice);
        }
    }

    private boolean addToPullingQueue(SystemPushNotice notice) {
        if (!mPullingQueue.contains(notice)) {
            mPullingQueue.add(notice);
            return true;
        } else {
            return false;
        }
    }

    private boolean removeFromPullingQueue(SystemPushNotice notice) {
        return mPullingQueue.remove(notice);
    }

    private void handlePullResult(Object object) {
        if (!(object instanceof PullResult)) {
            return;
        }

        final PullResult pullResult = (PullResult) object;

        L.v(TAG, "handlePullResult(), notice=%1$s, result=%2$d",
                pullResult.mNotice, pullResult.mResult);

        removeFromPullingQueue(pullResult.mNotice);

        if (pullResult.mResult != ConstantCode.ACCOUNT_OPERATION_NETWORK_ERROR) {
            deleteNotice(pullResult.mNotice);

            if (pullResult.mResult == ConstantCode.EXECUTE_RESULT_SUCCESS) {
                try {
                    PullSystemPushCommand.CommandResponse cmdResp
                            = new PullSystemPushCommand.CommandResponse(pullResult.mResponse);

                    if (cmdResp.requestSuccess()) {
                        notifyNewSystemPush(cmdResp.getSystemPush());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            // Network error, we should pull the system push message again.
            pullAllSystemPush();
        }
    }

    private void notifyNewSystemPush(SystemPush systemPush) {
        notifyListener(new NewSystemPushNotify(systemPush));
    }

    private static class PullResult {

        public final SystemPushNotice mNotice;
        public final int mResult;
        public final String mResponse;

        public PullResult(SystemPushNotice notice, int result, String response) {
            mNotice = notice;
            mResult = result;
            mResponse = response;
        }
    }

    private class PullSystemPushHandler implements ICommandResponseHandler {

        private final SystemPushNotice mNotice;

        public PullSystemPushHandler(SystemPushNotice notice) {
            mNotice = notice;
        }

        @Override
        public void onResponse(RequestCommand command, int result, String response) {
            Message message = mProcessHandler.obtainMessage(MSG_HANDLE_PULL_RESULT,
                    new PullResult(mNotice, result, response));
            mProcessHandler.sendMessage(message);
        }
    }

    private void notifyListener(IListenerNotifier notifier) {
        if (mListener != null) {
            try {
                notifier.notify(mListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class NewSystemPushNotify implements IListenerNotifier {

        private final SystemPush mSystemPush;

        public NewSystemPushNotify(SystemPush systemPush) {
            mSystemPush = systemPush;
        }

        @Override
        public void notify(IListener listener) {
            if (mSystemPush != null) {
                listener.onNewSystemPush(mSystemPush);
            }
        }
    }
}

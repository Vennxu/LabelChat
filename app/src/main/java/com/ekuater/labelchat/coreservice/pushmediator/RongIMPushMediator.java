package com.ekuater.labelchat.coreservice.pushmediator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.ekuater.labelchat.coreservice.ICoreServiceCallback;
import com.ekuater.labelchat.coreservice.immediator.RongIMPushMessage;
import com.ekuater.labelchat.util.L;

/**
 * @author LinYong
 */
public class RongIMPushMediator extends BasePushMediator {

    private static final String TAG = RongIMPushMediator.class.getSimpleName();

    private static final String ACTION_RECEIVE_PUSH_MESSAGE
            = "labelchat.intent.action.ACTION_RECEIVE_PUSH_MESSAGE";
    private static final String FIELD_PUSH_ID = RongIMPushMessage.FIELD_PUSH_ID;

    private static final int MSG_HANDLE_RECEIVE_PUSH_MESSAGE = 101;

    private final class ProcessHandler extends Handler {

        public ProcessHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_RECEIVE_PUSH_MESSAGE:
                    handleReceivePushMessage((String) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    private final Context mContext;
    private final Handler mHandler;
    private final BroadcastReceiver mPushReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            L.d(TAG, "onReceive(), intent=" + intent);

            if (action.equals(ACTION_RECEIVE_PUSH_MESSAGE)) {
                String pushId = intent.getStringExtra(FIELD_PUSH_ID);

                if (!TextUtils.isEmpty(pushId)) {
                    Message message = mHandler.obtainMessage(MSG_HANDLE_RECEIVE_PUSH_MESSAGE,
                            pushId);
                    mHandler.sendMessage(message);
                }
            }
        }
    };
    private boolean mConnected;

    public RongIMPushMediator(Context context, ICoreServiceCallback callback) {
        super(context, callback);
        mContext = context;
        mHandler = new ProcessHandler(mProcessThread.getLooper());
        mConnected = false;
    }

    private void handleReceivePushMessage(String pushId) {
        L.d(TAG, "handleReceivePushMessage(), pushId=" + pushId);

        SystemPushNotice notice = new SystemPushNotice(pushId);
        addNewSystemPushNotice(notice);
    }

    @Override
    public void init() {
    }

    @Override
    public void deInit() {
    }

    @Override
    protected void onConnect(String[] connectArgs) {
        if (mConnected) {
            return;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RECEIVE_PUSH_MESSAGE);
        mContext.registerReceiver(mPushReceiver, filter);
        mConnected = true;
    }

    @Override
    protected void onDisconnect() {
        if (!mConnected) {
            return;
        }

        mContext.unregisterReceiver(mPushReceiver);
        mConnected = false;
    }
}

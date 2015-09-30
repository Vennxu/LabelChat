package com.ekuater.labelchat.coreservice.pushmediator;

import android.content.Context;
import android.os.HandlerThread;

import com.ekuater.labelchat.coreservice.EventBusHub;
import com.ekuater.labelchat.coreservice.ICoreServiceCallback;
import com.ekuater.labelchat.coreservice.event.NewSystemPushEvent;
import com.ekuater.labelchat.datastruct.SystemPush;

import de.greenrobot.event.EventBus;

/**
 * @author LinYong
 */
public abstract class BasePushMediator {

    protected final Context mContext;
    protected final HandlerThread mProcessThread;
    private final SystemPushPullPool mPullPool;
    private final EventBus mCoreEventBus;

    public BasePushMediator(Context context, ICoreServiceCallback callback) {
        mContext = context;
        mCoreEventBus = EventBusHub.getCoreEventBus();
        mProcessThread = new HandlerThread("BasePushMediator");
        mProcessThread.start();
        SystemPushPullPool.IListener pullPoolListener
                = new SystemPushPullPool.IListener() {
            @Override
            public void onNewSystemPush(SystemPush systemPush) {
                notifyNewPushMessage(systemPush);
            }
        };
        mPullPool = new SystemPushPullPool(mContext, callback,
                mProcessThread.getLooper(), pullPoolListener);
    }

    private void notifyNewPushMessage(SystemPush systemPush) {
        mCoreEventBus.post(new NewSystemPushEvent(systemPush));
    }

    protected final void addNewSystemPushNotice(SystemPushNotice notice) {
        mPullPool.addNewNotice(notice);
    }

    public abstract void init();
    public abstract void deInit();


    public final void connect(String[] connectArgs) {
        onConnect(connectArgs);
        mPullPool.connect();
    }

    public final void disconnect() {
        onDisconnect();
        mPullPool.disconnect();
    }

    protected abstract void onConnect(String[] connectArgs);

    protected abstract void onDisconnect();
}

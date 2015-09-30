package com.ekuater.labelchat.notificationcenter;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.ekuater.labelchat.util.L;

/**
 * Notification center, single instance
 *
 * @author LinYong
 */
public final class NotificationCenter implements INotificationMediator {

    private static final String TAG = NotificationCenter.class.getSimpleName();

    private static NotificationCenter sInstance;

    private static synchronized void initInstance(Context context) {
        if (sInstance == null) {
            sInstance = new NotificationCenter(context.getApplicationContext());
        }
    }

    public static NotificationCenter getInstance(Context context) {
        if (sInstance == null) {
            initInstance(context);
        }

        return sInstance;
    }

    private INotificationMediator mMediator;
    private final Context mContext;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            L.d(TAG, "onServiceConnected()");
            mMediator = (INotificationMediator) service;
            try {
                service.linkToDeath(mDeathRecipient, 0);
            } catch (RemoteException rex) {
                L.e(TAG, "Failed to link to listener death");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMediator = null;
            checkInit();
        }
    };
    private final IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            unbindService();
            startService();
            bindService();
        }
    };

    private NotificationCenter(Context context) {
        mContext = context;
        mMediator = null;
        checkInit();
    }

    private void init() {
        startService();
        bindService();
    }

    public void checkInit() {
        if (mMediator == null) {
            init();
        }
    }

    private void startService() {
        mContext.startService(new Intent(mContext, NotificationService.class));
    }

    private void stopService() {
        mContext.stopService(new Intent(mContext, NotificationService.class));
    }

    private void bindService() {
        mContext.bindService(new Intent(mContext, NotificationService.class),
                mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindService() {
        IBinder binder = (IBinder) mMediator;
        binder.unlinkToDeath(mDeathRecipient, 0);
        mContext.unbindService(mServiceConnection);
    }

    @Override
    public void enterScenario(int scenario) {
        if (mMediator != null) {
            mMediator.enterScenario(scenario);
        }
    }

    @Override
    public void exitScenario(int scenario) {
        if (mMediator != null) {
            mMediator.exitScenario(scenario);
        }
    }

    public void enterMainUIScenario() {
        enterScenario(SCENARIO_MAIN_UI);
    }

    public void exitMainUIScenario() {
        exitScenario(SCENARIO_MAIN_UI);
    }

    public void enterChattingUIScenario() {
        enterScenario(SCENARIO_CHATTING_UI);
    }

    public void exitChattingUIScenario() {
        exitScenario(SCENARIO_CHATTING_UI);
    }

    public void exit() {
        unbindService();
        stopService();
        mMediator = null;

        // Cancel all notifications
        NotificationManager nM = (NotificationManager) mContext.getSystemService(
                Context.NOTIFICATION_SERVICE);
        nM.cancelAll();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        unbindService();
    }
}

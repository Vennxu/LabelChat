package com.ekuater.labelchat.notificationcenter;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.ekuater.labelchat.delegate.CoreServiceStarter;

/**
 * @author LinYong
 */
public class NotificationIntentService extends Service {

    private static final int MSG_PROCESS_NOTIFICATION_INTENT = 101;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PROCESS_NOTIFICATION_INTENT:
                    processNotificationIntent(msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    private class StartListener implements CoreServiceStarter.OnStartListener {

        private final Intent mServiceIntent;

        public StartListener(Intent serviceIntent) {
            mServiceIntent = serviceIntent;
        }

        @Override
        public void onStarted() {
            Message message = mHandler.obtainMessage(MSG_PROCESS_NOTIFICATION_INTENT,
                    mServiceIntent);
            mHandler.sendMessage(message);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CoreServiceStarter.start(this, new StartListener(intent));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void processNotificationIntent(Object object) {
        if (object instanceof Intent) {
            Intent serviceIntent = (Intent) object;
            NotificationIntent.processServiceIntent(this, serviceIntent);
        }
    }
}

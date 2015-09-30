package com.ekuater.labelchat;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.ekuater.labelchat.delegate.CoreServiceStarter;

/**
 * @author LinYong
 */
public class BootCompleteService extends Service {

    private Handler mHandler;
    private final CoreServiceStarter.OnStartListener mStartListener
            = new CoreServiceStarter.OnStartListener() {
        @Override
        public void onStarted() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onCoreServiceStarted();
                }
            });
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        CoreServiceStarter.start(this, mStartListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void onCoreServiceStarted() {
        finish();
    }

    private void finish() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                stopSelf();
            }
        });
    }
}

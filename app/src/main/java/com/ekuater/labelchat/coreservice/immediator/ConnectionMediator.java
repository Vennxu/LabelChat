
package com.ekuater.labelchat.coreservice.immediator;

import android.content.Context;
import android.os.Handler;

import com.ekuater.labelchat.coreservice.INetworkListener;
import com.ekuater.labelchat.coreservice.NetworkMonitor;

/**
 * @author LinYong
 */
public class ConnectionMediator {

    private static final int RECONNECT_DELAY = 5 * 1000; // 5 seconds

    private final Context mContext;
    private final IConnectionCallback mCallback;
    private final Handler mHandler;
    private final NetworkMonitor mNetworkMonitor;

    private final INetworkListener mNetworkListener = new INetworkListener() {

        @Override
        public void networkAvailableChanged(boolean networkAvailable) {
            handleNetworkChanged(networkAvailable);
        }
    };

    private boolean mRunning = false;

    public ConnectionMediator(Context context, IConnectionCallback callback, Handler handler) {
        mContext = context;
        mCallback = callback;
        mHandler = handler;
        mNetworkMonitor = new NetworkMonitor(mContext, mNetworkListener);
    }

    public synchronized void start() {
        if (mRunning) {
            return;
        }
        mNetworkMonitor.start();
        mRunning = true;
    }

    public synchronized void stop() {
        if (!mRunning) {
            return;
        }
        mNetworkMonitor.stop();
        cancelReconnectDelay();
        mRunning = false;
    }

    public void onConnectionClosedOnError() {
        handleReconnectDelay();
    }

    public void onConnectionConnected() {
        cancelReconnectDelay();
    }

    private void handleNetworkChanged(boolean networkAvailable) {
        if (networkAvailable && !isConnConnected()) {
            notifyReconnectNeeded();
        }
    }

    private boolean isConnConnected() {
        return mCallback.isConnectionConnected();
    }

    private boolean isNetworkAvailable() {
        return mNetworkMonitor.isNetworkAvailable();
    }

    private synchronized void notifyReconnectNeeded() {
        mCallback.needReconnect();
    }

    private final Runnable mReconnectDelayRunnable = new Runnable() {

        @Override
        public void run() {
            if (isNetworkAvailable() && !isConnConnected()) {
                notifyReconnectNeeded();
            }
        }
    };

    private void handleReconnectDelay() {
        mHandler.postDelayed(mReconnectDelayRunnable, RECONNECT_DELAY);
    }

    private void cancelReconnectDelay() {
        mHandler.removeCallbacks(mReconnectDelayRunnable);
    }
}

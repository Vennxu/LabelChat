
package com.ekuater.labelchat.coreservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * This class monitor network state change, and then notify to listener.
 *
 * @author LinYong
 */
public class NetworkMonitor {

    private final Context mContext;
    private final ConnectivityManager mConnectivityManager;
    private NetworkInfo mCurrentNetworkInfo;
    private INetworkListener mListener;
    private boolean mRunning = false;

    private BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                handleConnectivityChanged();
            }
        }
    };

    public NetworkMonitor(Context context, INetworkListener listener) {
        mContext = context;
        mListener = listener;
        mConnectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        mCurrentNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
    }

    public synchronized void start() {
        if (!mRunning) {
            mContext.registerReceiver(mConnectivityReceiver, new IntentFilter(
                    ConnectivityManager.CONNECTIVITY_ACTION));
            mRunning = true;
        }
    }

    public synchronized void stop() {
        if (mRunning) {
            mContext.unregisterReceiver(mConnectivityReceiver);
            mRunning = false;
        }
    }

    public boolean isNetworkAvailable() {
        return mCurrentNetworkInfo != null && mCurrentNetworkInfo.isAvailable();
    }

    private synchronized void handleConnectivityChanged() {
        mCurrentNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        notifyNetworkChanged(isNetworkAvailable());
    }

    private void notifyNetworkChanged(boolean networkAvailable) {
        if (mListener != null) {
            mListener.networkAvailableChanged(networkAvailable);
        }
    }
}

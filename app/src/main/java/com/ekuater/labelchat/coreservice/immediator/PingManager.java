
package com.ekuater.labelchat.coreservice.immediator;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

import com.ekuater.labelchat.BuildConfig;

/**
 * Send ping message to server to keep IM connection in some interval
 *
 * @author LinYong
 */
public class PingManager {

    public interface IPingListener {
        public void pingTick();
    }

    private static final String ACTION_PING_ALARM = BuildConfig.APPLICATION_ID + ".ACTION_PING_ALARM";
    private static final long DEFAULT_PING_INTERVAL = 5 * 1000L; // 5 seconds interval

    private static final class AlarmReceiver extends BroadcastReceiver {
        private final PingManager mPingManager;

        public AlarmReceiver(PingManager pingManager) {
            mPingManager = pingManager;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(ACTION_PING_ALARM)) {
                mPingManager.notifyPingTick();
            }
        }
    }

    private Context mContext;
    private final AlarmManager mAlarmManager;
    private PendingIntent mPi;
    private long mPingInterval;
    private BroadcastReceiver mAlarmReceiver;
    private IPingListener mListener;
    private boolean mRunning = false;

    public PingManager(Context context, IPingListener listener) {
        mContext = context;
        mListener = listener;
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        mPingInterval = DEFAULT_PING_INTERVAL;
        mPi = PendingIntent.getService(mContext, 0, new Intent(ACTION_PING_ALARM),
                PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmReceiver = new AlarmReceiver(this);
    }

    public synchronized void start() {
        if (mRunning) {
            return;
        }
        mContext.registerReceiver(mAlarmReceiver, new IntentFilter(ACTION_PING_ALARM));
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + mPingInterval, mPingInterval, mPi);
        mRunning = true;
    }

    public synchronized void stop() {
        if (!mRunning) {
            return;
        }
        mContext.unregisterReceiver(mAlarmReceiver);
        mAlarmManager.cancel(mPi);
        mRunning = false;
    }

    public long getPingInterval() {
        return mPingInterval;
    }

    public void setPingInterval(long pingInterval) {
        mPingInterval = pingInterval;
    }

    private void notifyPingTick() {
        if (mListener != null) {
            mListener.pingTick();
        }
    }
}

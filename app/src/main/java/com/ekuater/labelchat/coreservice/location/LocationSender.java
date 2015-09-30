
package com.ekuater.labelchat.coreservice.location;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

import com.ekuater.labelchat.BuildConfig;
import com.ekuater.labelchat.command.BaseCommand;
import com.ekuater.labelchat.command.account.UpdatePositionCommand;
import com.ekuater.labelchat.coreservice.ICoreServiceCallback;
import com.ekuater.labelchat.coreservice.command.ICommandResponseHandler;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.RequestCommand;
import com.ekuater.labelchat.util.L;
import com.ekuater.labelchat.util.location.ILocationListener;
import com.ekuater.labelchat.util.location.LocationManager;

/**
 * This class inspects the device's position information at intervals and
 * transmits it to sever
 *
 * @author LinYong
 */
public class LocationSender {

    private static final String TAG = LocationSender.class.getSimpleName();
    private static final double LOCATION_SEND_DISTANCE = 20.0D;
    private static final long ONE_MINUTE = 60 * 1000;
    private static final long DEFAULT_SEND_INTERVAL = 20 * ONE_MINUTE; // 20 minutes
    private static final String ACTION_LOCATION_SEND_ALARM
            = BuildConfig.APPLICATION_ID + ".ACTION_LOCATION_SEND_ALARM";

    private final Context mContext;
    private final ICoreServiceCallback mCallback;
    private final LocationInfo mLocation = new LocationInfo();
    private LocationInfo mLastSendLocation = null;
    private boolean mLocating = false;
    private boolean mSending = false;
    private LocationManager mLocationManager;
    private ILocationListener mLocationListener = new ILocationListener() {

        @Override
        public void onLocationChanged(LocationInfo location) {
            setLocation(location);
        }
    };

    private final class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(ACTION_LOCATION_SEND_ALARM)) {
                handleSendLocation();
            }
        }
    }

    private AlarmManager mAlarmManager;
    private BroadcastReceiver mAlarmReceiver;
    private PendingIntent mPi;

    public LocationSender(Context context, ICoreServiceCallback callback) {
        mContext = context;
        mCallback = callback;
        mLocationManager = LocationManager.getInstance(mContext);
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        mAlarmReceiver = new AlarmReceiver();
        mPi = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_LOCATION_SEND_ALARM),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public synchronized void startLocating() {
        if (!mLocating) {
            mLocationManager.registerListener(mLocationListener);
            mLocating = true;
        }
    }

    public synchronized void stopLocating() {
        if (mLocating) {
            mLocationManager.unregisterListener(mLocationListener);
            mLocating = false;
        }
    }

    public synchronized void startSending() {
        if (!mSending) {
            final long interval = getSendInterval();
            mContext.registerReceiver(mAlarmReceiver, new IntentFilter(ACTION_LOCATION_SEND_ALARM));
            mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + ONE_MINUTE, interval, mPi);
            mSending = true;
        }
    }

    public synchronized void stopSending() {
        if (mSending) {
            mContext.unregisterReceiver(mAlarmReceiver);
            mAlarmManager.cancel(mPi);
            mSending = false;
        }
    }

    public LocationInfo getLocation() {
        LocationInfo temp = new LocationInfo();
        synchronized (mLocation) {
            temp.set(mLocation);
        }
        return temp;
    }

    private void setLocation(LocationInfo location) {
        synchronized (mLocation) {
            mLocation.set(location);
        }
    }

    private long getSendInterval() {
        return DEFAULT_SEND_INTERVAL;
    }

    private void handleSendLocation() {
        final LocationInfo location = getLocation();

        L.d(TAG, "handleSendLocation(), last location=%1$s, current location=%2$s",
                mLastSendLocation, location);

        if (mLastSendLocation != null) {
            final double distance = mLastSendLocation.getDistance(location);

            L.d(TAG, "handleSendLocation(), distance=%1$f", distance);

            if (distance < LOCATION_SEND_DISTANCE) {
                L.d(TAG, "handleSendLocation(), location changed in acceptable distance");
                // Do not need to send location
                return;
            }
        }

        UpdatePositionCommand command
                = (UpdatePositionCommand) preTreatCommand(new UpdatePositionCommand());
        ICommandResponseHandler handler = new ICommandResponseHandler() {
            @Override
            public void onResponse(RequestCommand command, int result,
                                   String response) {
            }
        };
        command.putParamLocation(location);
        executeCommand(command, handler);
        mLastSendLocation = location;
    }

    private BaseCommand preTreatCommand(BaseCommand command) {
        return mCallback.preTreatCommand(command);
    }

    private void executeCommand(BaseCommand command, ICommandResponseHandler handler) {
        mCallback.executeCommand(command.toRequestCommand(), handler);
    }
}

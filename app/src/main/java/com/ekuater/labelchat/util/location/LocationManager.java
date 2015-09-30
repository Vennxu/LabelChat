
package com.ekuater.labelchat.util.location;

import android.content.Context;

import com.ekuater.labelchat.datastruct.LocationInfo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * LBS manager
 *
 * @author LinYong
 */
public final class LocationManager {

    private interface ILocationListenerNotifier {

        public void notify(ILocationListener listener);
    }

    private static LocationManager mInstance;

    private final Context mContext;
    private LocationInfo mCurrentLocation;
    private boolean mLocated = false;
    private ILocation mLocationImpl;
    private final ArrayList<WeakReference<ILocationListener>> mListeners = new ArrayList<WeakReference<ILocationListener>>();

    private ILocationListener mLocationListener = new ILocationListener() {

        @Override
        public void onLocationChanged(final LocationInfo location) {
            mCurrentLocation.set(location);
            notifyLocationListener(new LocationChangedNotifier(mCurrentLocation));
            mLocated = true;
        }
    };

    public static LocationManager getInstance(final Context context) {
        if (mInstance == null) {
            mInstance = new LocationManager(context);
        }

        return mInstance;
    }

    private LocationManager(final Context context) {
        mContext = context;
        initialize();
    }

    private void initialize() {
        mCurrentLocation = new LocationInfo();
        mLocationImpl = new LocationImpl(mContext);
        mLocationImpl.registerListener(mLocationListener);
        mLocationImpl.startLocation();
    }

    private void notifyLocationListener(ILocationListenerNotifier notifier) {
        for (WeakReference<ILocationListener> ref : mListeners) {
            ILocationListener listener = ref.get();
            if (listener != null) {
                try {
                    notifier.notify(listener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void registerListener(final ILocationListener listener) {
        for (WeakReference<ILocationListener> ref : mListeners) {
            if (ref.get() == listener) {
                return;
            }
        }

        mListeners.add(new WeakReference<ILocationListener>(listener));
        unregisterListener(null);

        if (mLocated && listener != null) {
            listener.onLocationChanged(mCurrentLocation);
        }
    }

    public void unregisterListener(final ILocationListener listener) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            if (mListeners.get(i).get() == listener) {
                mListeners.remove(i);
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mLocationImpl.stopLocation();
        mLocationImpl.unregisterListener();
    }

    private static class LocationChangedNotifier implements ILocationListenerNotifier {

        private final LocationInfo mLocation;

        public LocationChangedNotifier(LocationInfo location) {
            mLocation = location;
        }

        @Override
        public void notify(ILocationListener listener) {
            listener.onLocationChanged(mLocation);
        }
    }
}


package com.ekuater.labelchat.util.location;

import android.content.Context;
import android.os.Bundle;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.ekuater.labelchat.datastruct.LocationInfo;

/**
 * LBS implement by amap location api
 * 
 * @author LinYong
 */
public class LocationImpl implements ILocation {

    private final Context mContext;
    private LocationInfo mLocation;
    private ILocationListener mListener;

    private LocationManagerProxy mAMapLocManager;

    private AMapLocationListener mAMapLocationListener = new AMapLocationListener() {

        @Override
        public void onLocationChanged(android.location.Location location) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onLocationChanged(AMapLocation location) {
            mLocation.set(location.getLongitude(), location.getLatitude());

            if (mListener != null) {
                mListener.onLocationChanged(mLocation);
            }
        }
    };

    /* package */LocationImpl(final Context context) {
        mContext = context;
        mLocation = new LocationInfo();
    }

    @Override
    public LocationInfo getLocation() {
        return mLocation;
    }

    @Override
    public void registerListener(ILocationListener listener) {
        mListener = listener;
    }

    @Override
    public void unregisterListener() {
        mListener = null;
    }

    @Override
    public void startLocation() {
        if (mAMapLocManager != null) {
            stopLocation();
        }

        mAMapLocManager = LocationManagerProxy.getInstance(mContext);
        mAMapLocManager.setGpsEnable(false);
        mAMapLocManager.requestLocationData(LocationProviderProxy.AMapNetwork, 2000, 10,
                mAMapLocationListener);
    }

    @Override
    public void stopLocation() {
        if (mAMapLocManager != null) {
            mAMapLocManager.removeUpdates(mAMapLocationListener);
            mAMapLocManager.destroy();
        }
        mAMapLocManager = null;
    }
}

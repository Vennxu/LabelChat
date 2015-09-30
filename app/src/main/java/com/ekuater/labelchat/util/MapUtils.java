package com.ekuater.labelchat.util;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.ekuater.labelchat.datastruct.LocationInfo;

/**
 * Created by Leo on 2015/1/8.
 * AMap Utilities
 *
 * @author LinYong
 */
public final class MapUtils {

    public static LatLng toLatLng(LocationInfo location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public static LatLonPoint toLatLonPoint(LocationInfo location) {
        return new LatLonPoint(location.getLatitude(), location.getLongitude());
    }
}

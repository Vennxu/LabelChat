package com.ekuater.labelchat.util;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;
import android.util.Pair;

import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.StreetNumber;
import com.ekuater.labelchat.datastruct.LocationInfo;

/**
 * Created by Leo on 2015/1/12.
 * Geo code search utility
 *
 * @author LinYong
 */
public class GeocodeSearcher {

    private static final String TAG = GeocodeSearcher.class.getSimpleName();

    public static class SearchAddress {

        public final String province;     // 省、直辖市名称
        public final String city;         // 城市名称。直辖市的名称参见省名称，此项为空
        public final String district;     // 区（县）名称
        public final String township;     // 乡镇名称
        public final String street;       // 街道名称
        public final String number;       // 门牌号码
        public final String neighborhood; // 社区名称
        public final String building;     // 建筑物名称

        public SearchAddress(String province,
                             String city,
                             String district,
                             String township,
                             String street,
                             String number,
                             String neighborhood,
                             String building) {
            this.province = province;
            this.city = city;
            this.district = district;
            this.township = township;
            this.street = street;
            this.number = number;
            this.neighborhood = neighborhood;
            this.building = building;
        }
    }

    public interface AddressObserver {
        public void onSearch(boolean success, SearchAddress address);
    }

    private static final int MSG_GET_ADDRESS_FROM_CACHE = 101;
    private static final int MSG_MSG_SYNC_RE_GEOCODE_SEARCHED = 102;

    private static GeocodeSearcher sSingleton;

    private static synchronized void initInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new GeocodeSearcher(context.getApplicationContext());
        }
    }

    public static GeocodeSearcher getInstance(Context context) {
        if (sSingleton == null) {
            initInstance(context);
        }
        return sSingleton;
    }

    private final GeocodeSearch mSearch;
    private final Handler mHandler;
    private final LruCache<String, SearchAddress> mAddressCache;

    private GeocodeSearcher(Context context) {
        final Impl impl = new Impl();
        mSearch = new GeocodeSearch(context);
        mHandler = new Handler(context.getMainLooper(), impl);
        mAddressCache = new LruCache<>(50);
    }

    public void searchAddress(LocationInfo location, AddressObserver observer) {
        if (location == null) {
            L.w(TAG, "location is null, just return");
            return;
        }
        if (observer == null) {
            L.w(TAG, "observer is null, just return");
            return;
        }

        SearchAddress address = getFromCache(location);
        if (address != null) {
            mHandler.obtainMessage(MSG_GET_ADDRESS_FROM_CACHE,
                    Pair.create(address, observer)).sendToTarget();
            return;
        }

        ThreadPool.getDefault().execute(new QueryTask(location, observer));
    }

    private void handleGetAddressFromCache(Pair<SearchAddress, AddressObserver> cacheResult) {
        cacheResult.second.onSearch(true, cacheResult.first);
    }

    private void handleSyncGetAddress(Pair<SearchAddress, AddressObserver> cacheResult) {
        cacheResult.second.onSearch(cacheResult.first != null, cacheResult.first);
    }

    private SearchAddress convertAddress(RegeocodeAddress regeocodeAddress) {
        final StreetNumber streetNumber = regeocodeAddress.getStreetNumber();
        final String street = (streetNumber != null) ? streetNumber.getStreet() : "";
        final String number = (streetNumber != null) ? streetNumber.getNumber() : "";

        return new SearchAddress(
                regeocodeAddress.getProvince(),
                regeocodeAddress.getCity(),
                regeocodeAddress.getDistrict(),
                regeocodeAddress.getTownship(),
                street, number,
                regeocodeAddress.getNeighborhood(),
                regeocodeAddress.getBuilding()
        );
    }

    private SearchAddress getFromCache(LocationInfo location) {
        synchronized (mAddressCache) {
            return mAddressCache.get(keyOfLocation(location));
        }
    }

    private void putToCache(LocationInfo location, SearchAddress address) {
        synchronized (mAddressCache) {
            mAddressCache.put(keyOfLocation(location), address);
        }
    }

    private String keyOfLocation(LocationInfo location) {
        return String.valueOf(location.getLongitude()) + ","
                + String.valueOf(location.getLatitude());
    }

    private class Impl implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            boolean handled = true;

            switch (msg.what) {
                case MSG_GET_ADDRESS_FROM_CACHE:
                    //noinspection unchecked
                    handleGetAddressFromCache((Pair<SearchAddress, AddressObserver>) msg.obj);
                    break;
                case MSG_MSG_SYNC_RE_GEOCODE_SEARCHED:
                    //noinspection unchecked
                    handleSyncGetAddress((Pair<SearchAddress, AddressObserver>) msg.obj);
                    break;
                default:
                    handled = false;
                    break;
            }

            return handled;
        }
    }

    private class QueryTask implements Runnable {

        private final LocationInfo location;
        private final AddressObserver observer;

        public QueryTask(LocationInfo location, AddressObserver observer) {
            this.location = location;
            this.observer = observer;
        }

        @Override
        public void run() {
            try {
                RegeocodeQuery query = new RegeocodeQuery(MapUtils.toLatLonPoint(this.location),
                        200, GeocodeSearch.AMAP);
                RegeocodeAddress regeocodeAddress = mSearch.getFromLocation(query);
                SearchAddress address = regeocodeAddress != null
                        ? convertAddress(regeocodeAddress) : null;

                if (address != null) {
                    putToCache(location, address);
                }
                mHandler.obtainMessage(MSG_MSG_SYNC_RE_GEOCODE_SEARCHED,
                        Pair.create(address, this.observer)).sendToTarget();
            } catch (Exception e) {
                L.w(TAG, e);
            }
        }
    }
}

package com.ekuater.labelchat.ui.fragment.throwphoto;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.ThrowPhoto;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.ThrowPhotoManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.util.MapUtils;

/**
 * Created by Leo on 2015/1/8.
 *
 * @author LinYong
 */
public class NearByThrowPhotosFragment extends Fragment
        implements AMap.OnMarkerClickListener {

    private static final int THROW_PHOTO_LIMIT_COUNT = 20;

    private static final int MSG_GET_NEAR_BY_THROW_PHOTO_RESULT = 101;

    private ThrowPhotoManager mThrowPhotoManager;
    private LocationInfo mMyLocation;
    private MapView mMapView;
    private AMap mAMap;
    private ImageView mLoadingImage;

    private final PhotoMarker.OnClickListener mMarkerClickListener
            = new PhotoMarker.OnClickListener() {
        @Override
        public void onClick(ThrowPhoto throwPhoto) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(ThrowPhotoShowFragment.EXTRA_THROW_PHOTO,
                    throwPhoto);
            arguments.putBoolean(ThrowPhotoShowFragment.EXTRA_SCENARIO_PICK, true);
            UILauncher.launchFragmentInNewActivity(getActivity(),
                    ThrowPhotoShowFragment.class, arguments);
        }
    };

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GET_NEAR_BY_THROW_PHOTO_RESULT:
                    handleGetNearByThrowPhotosResult((ThrowPhoto[]) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        mThrowPhotoManager = ThrowPhotoManager.getInstance(activity);
        mMyLocation = AccountManager.getInstance(activity).getLocation();

        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.pick_photo);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_near_by_throw_photos, container, false);
        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mAMap = mMapView.getMap();
        mLoadingImage = (ImageView) view.findViewById(R.id.loading);
        setupMap();
        getNearByThrowPhotos();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMapView.onDestroy();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Object object = marker.getObject();

        if (object instanceof PhotoMarker) {
            PhotoMarker photoMarker = (PhotoMarker) object;
            photoMarker.onClick();
            return true;
        }

        return false;
    }

    private void setupMap() {
        LatLng latLng = MapUtils.toLatLng(mMyLocation);
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(
                new CameraPosition(latLng, 18, 30, 0));
        mAMap.moveCamera(update);
        mAMap.setOnMarkerClickListener(this);
    }

    private void getNearByThrowPhotos() {
        startLoadAnimation();
        mThrowPhotoManager.getNearByThrowPhotos(
                new ThrowPhotoManager.ThrowPhotoQueryObserver() {
                    @Override
                    public void onQueryResult(ThrowPhotoManager.ResultCode result,
                                              ThrowPhoto[] throwPhotos) {
                        Message msg = mHandler.obtainMessage(MSG_GET_NEAR_BY_THROW_PHOTO_RESULT,
                                throwPhotos);
                        mHandler.sendMessage(msg);
                    }
                });
    }

    private void handleGetNearByThrowPhotosResult(ThrowPhoto[] throwPhotos) {
        setupPhotoMarkers(throwPhotos);
        stopLoadAnimation();
    }

    private void startLoadAnimation() {
        if (mLoadingImage != null) {
            mLoadingImage.setVisibility(View.VISIBLE);

            Drawable drawable = mLoadingImage.getDrawable();
            if (drawable instanceof AnimationDrawable) {
                AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
                animationDrawable.start();
            }
        }
    }

    private void stopLoadAnimation() {
        if (mLoadingImage != null) {
            Drawable drawable = mLoadingImage.getDrawable();
            if (drawable instanceof AnimationDrawable) {
                AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
                animationDrawable.stop();
            }

            mLoadingImage.setVisibility(View.GONE);
        }
    }

    private void setupPhotoMarkers(ThrowPhoto[] throwPhotos) {
        mAMap.clear();

        if (throwPhotos != null && throwPhotos.length > 0) {
            // limit throw photos count in 20 first.
            final int limitCount = THROW_PHOTO_LIMIT_COUNT;
            if (throwPhotos.length > limitCount) {
                ThrowPhoto[] tmpPhotos = new ThrowPhoto[limitCount];
                System.arraycopy(throwPhotos, 0, tmpPhotos, 0, limitCount);
                throwPhotos = tmpPhotos;
            }

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (ThrowPhoto throwPhoto : throwPhotos) {
                new PhotoMarker(getActivity(), throwPhoto, mAMap,
                        mMarkerClickListener).addMarker();
                builder = builder.include(MapUtils.toLatLng(throwPhoto.getLocation()));
            }
            mAMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 10));
        }
    }
}

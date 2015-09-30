package com.ekuater.labelchat.ui.fragment.throwphoto;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ThrowPhoto;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.util.MapUtils;

/**
 * Created by Leo on 2015/1/8.
 *
 * @author LinYong
 */
public class MyThrowPhotoDetailFragment extends Fragment
        implements AMap.OnMarkerClickListener {

    public static final String EXTRA_MY_THROW_PHOTO = "extra_my_throw_photo";

    private ThrowPhoto mThrowPhoto;
    private MapView mMapView;
    private AMap mAMap;

    private final PhotoMarker.OnClickListener mMarkerClickListener
            = new PhotoMarker.OnClickListener() {
        @Override
        public void onClick(ThrowPhoto throwPhoto) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(ThrowPhotoShowFragment.EXTRA_THROW_PHOTO,
                    throwPhoto);
            UILauncher.launchFragmentInNewActivity(getActivity(),
                    ThrowPhotoShowFragment.class, arguments);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        parseArguments();

        final ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.my_throw_photos_where);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_throw_photo_detail, container, false);
        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mAMap = mMapView.getMap();
        setupMap();
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

    private void parseArguments() {
        Bundle args = getArguments();
        mThrowPhoto = args.getParcelable(EXTRA_MY_THROW_PHOTO);
    }

    private void setupMap() {
        LatLng latLng = MapUtils.toLatLng(mThrowPhoto.getLocation());
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(
                new CameraPosition(latLng, 18, 30, 0));
        mAMap.moveCamera(update);
        mAMap.setOnMarkerClickListener(this);
        new PhotoMarker(getActivity(), mThrowPhoto, mAMap,
                mMarkerClickListener).addMarker();
    }
}

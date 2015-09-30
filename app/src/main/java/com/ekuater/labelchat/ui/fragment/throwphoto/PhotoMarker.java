package com.ekuater.labelchat.ui.fragment.throwphoto;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ThrowPhoto;
import com.ekuater.labelchat.delegate.ShortUrlImageLoadListener;
import com.ekuater.labelchat.delegate.ThrowPhotoManager;
import com.ekuater.labelchat.delegate.imageloader.LoadFailType;
import com.ekuater.labelchat.util.BmpUtils;
import com.ekuater.labelchat.util.L;
import com.ekuater.labelchat.util.MapUtils;

/**
 * Created by Leo on 2015/1/9.
 *
 * @author LinYong
 */
public class PhotoMarker {

    private static final String TAG = PhotoMarker.class.getSimpleName();

    public interface OnClickListener {
        public void onClick(ThrowPhoto throwPhoto);
    }

    private ThrowPhoto mThrowPhoto;
    private ThrowPhotoManager mManager;
    private LayoutInflater mInflater;
    private AMap mAMap;
    private OnClickListener mClickListener;
    private Marker mMarker;
    private int mPhotoMaxWidth;
    private int mPhotoMaxHeight;

    public PhotoMarker(Context context, ThrowPhoto throwPhoto, AMap aMap,
                       OnClickListener onClickListener) {
        Resources res = context.getResources();
        mThrowPhoto = throwPhoto;
        mManager = ThrowPhotoManager.getInstance(context);
        mInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mAMap = aMap;
        mClickListener = onClickListener;
        mPhotoMaxWidth = res.getDimensionPixelSize(R.dimen.throw_photo_marker_max_width);
        mPhotoMaxHeight = res.getDimensionPixelSize(R.dimen.throw_photo_marker_max_height);
    }

    public void addMarker() {
        L.v(TAG, "addMarker(), throwPhoto:" + mThrowPhoto.getId());
        Bitmap photoBitmap = getDisplayPhoto();
        addMarkerInternal(photoBitmap);
        if (photoBitmap != null) {
            photoBitmap.recycle();
        }
    }

    public void onClick() {
        L.v(TAG, "onClick(), throwPhoto:" + mThrowPhoto.getId());
        if (mClickListener != null) {
            mClickListener.onClick(mThrowPhoto);
        }
    }

    private void addMarkerInternal(Bitmap photoBitmap) {
        try {
            destroyMarker();

            @SuppressLint("InflateParams")
            View view = mInflater.inflate(R.layout.throw_photo_marker, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.display_photo);
            if (photoBitmap != null) {
                imageView.setImageBitmap(photoBitmap);
            }

            mMarker = mAMap.addMarker(new MarkerOptions()
                    .position(MapUtils.toLatLng(mThrowPhoto.getLocation()))
                    .icon(BitmapDescriptorFactory.fromView(view))
                    .anchor(0.5F, 1.0F)
                    .draggable(false));
            mMarker.setObject(this);
        } catch (Throwable t) {
            L.w(TAG, t);
        }
    }

    private void destroyMarker() {
        if (mMarker != null) {
            mMarker.destroy();
            mMarker = null;
        }
    }

    private Bitmap getDisplayPhoto() {
        Bitmap bitmap = mManager.getDisplayPhotoBitmap(mThrowPhoto.getDisplayPhoto(),
                new ShortUrlImageLoadListener() {
                    @Override
                    public void onLoadFailed(String url, LoadFailType loadFailType) {
                        destroyMarker();
                    }

                    @Override
                    public void onLoadComplete(String url, Bitmap loadedImage) {
                        L.v(TAG, "onLoadComplete(), throwPhoto:" + mThrowPhoto.getId());
                        Bitmap photoBitmap = convertPhotoBitmap(loadedImage);
                        addMarkerInternal(photoBitmap);
                        if (photoBitmap != null) {
                            photoBitmap.recycle();
                        }
                    }
                });
        return convertPhotoBitmap(bitmap);
    }

    private Bitmap convertPhotoBitmap(Bitmap bitmap) {
        Bitmap photoBitmap = null;

        try {
            photoBitmap = (bitmap == null) ? null
                    : BmpUtils.zoomDownBitmap(bitmap, mPhotoMaxWidth, mPhotoMaxHeight);
        } catch (Throwable t) {
            L.w(TAG, t);
        }

        return photoBitmap;
    }
}

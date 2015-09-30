package com.ekuater.labelchat.ui.fragment.album;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.AlbumPhoto;
import com.ekuater.labelchat.delegate.AlbumManager;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Leo on 2015/3/24.
 *
 * @author LinYong
 */
public class PhotoDisplayFragment extends Fragment {

    private AlbumManager mAlbumManager;
    private AlbumPhoto mAlbumPhoto;

    public void setAlbumPhoto(AlbumPhoto photo) {
        mAlbumPhoto = photo;
    }

    public AlbumPhoto getAlbumPhoto() {
        return mAlbumPhoto;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        mAlbumManager = AlbumManager.getInstance(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_photo_display, container, false);
        PhotoView photoView = (PhotoView) rootView.findViewById(R.id.photo);
        PhotoViewListener listener = new PhotoViewListener();
        photoView.setOnViewTapListener(listener);
        photoView.setOnPhotoTapListener(listener);
        if (mAlbumPhoto != null) {
            mAlbumManager.displayPhoto(mAlbumPhoto.getPhoto(), photoView,
                    R.drawable.pic_loading);
        }
        return rootView;
    }

    private void finish() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    private class PhotoViewListener implements PhotoViewAttacher.OnViewTapListener,
            PhotoViewAttacher.OnPhotoTapListener {

        @Override
        public void onPhotoTap(View view, float x, float y) {
            finish();
        }

        @Override
        public void onViewTap(View view, float x, float y) {
            finish();
        }
    }
}

package com.ekuater.labelchat.ui.fragment.image;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.AlbumPhoto;
import com.ekuater.labelchat.delegate.AlbumManager;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.ui.util.MiscUtils;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Leo on 2015/5/22.
 *
 */
public class ImageDisplayFragment extends Fragment {

    private String mImage;
    private PhotoView photoView;

    public void setAlbumPhoto(String image) {
        mImage = image;
    }

    public String getImage() {
        return mImage;
    }

    public Bitmap getImageBitmap(){
        Drawable drawable = photoView.getDrawable();
        if (drawable == null){
            return null;
        }
        BitmapDrawable bd = (BitmapDrawable) drawable;
        return bd.getBitmap();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_photo_display, container, false);
        photoView = (PhotoView) rootView.findViewById(R.id.photo);
        PhotoViewListener listener = new PhotoViewListener();
        photoView.setOnViewTapListener(listener);
        photoView.setOnPhotoTapListener(listener);
        if (mImage != null) {
            MiscUtils.showLabelStoryImage(AvatarManager.getInstance(getActivity()), mImage, photoView, R.drawable.pic_loading);
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

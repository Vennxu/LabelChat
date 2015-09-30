package com.ekuater.labelchat.ui.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ShortUrlImageLoadListener;
import com.ekuater.labelchat.delegate.imageloader.LoadFailType;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * @author FanChong
 */
public class ShowAvatarImageFragment extends Fragment implements Handler.Callback {

    public static final String SHOW_FRIEND_AVATAR_IMAGE = "show_friend_avatar_image";

    private static final int MSG_LOAD_AVATAR_IMAGE = 1;

    private Handler mHandler;
    private PhotoView mAvatarImage;
    private String mAvatarUrl;
    private ProgressBar mProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mHandler = new Handler(this);
        parseArguments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_avatar_image, container, false);
        mAvatarImage = (PhotoView) view.findViewById(R.id.photo);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
        PhotoViewListener listener = new PhotoViewListener();
        mAvatarImage.setOnViewTapListener(listener);
        mAvatarImage.setOnPhotoTapListener(listener);

        Bitmap bitmap = AvatarManager.getInstance(getActivity()).getAvatarBitmap(mAvatarUrl,
                new ShortUrlImageLoadListener() {
                    @Override
                    public void onLoadFailed(String url, LoadFailType loadFailType) {
                        loadAvatarImage(null);
                    }

                    @Override
                    public void onLoadComplete(String url, Bitmap loadedImage) {
                        loadAvatarImage(loadedImage);
                    }
                });
        if (bitmap != null) {
            loadAvatarImage(bitmap);
        }
        return view;
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case MSG_LOAD_AVATAR_IMAGE:
                handleLoadImage(msg.obj);
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }

    private void loadAvatarImage(Bitmap bitmap) {
        Message message = mHandler.obtainMessage(MSG_LOAD_AVATAR_IMAGE, bitmap);
        mHandler.sendMessage(message);
    }

    private void handleLoadImage(Object object) {
        mProgressBar.setVisibility(View.GONE);

        if (object instanceof Bitmap) {
            setAvatarImage((Bitmap) object);
        } else if (object == null) {
            setAvatarImage(null);
        }
    }

    private void setAvatarImage(Bitmap bitmap) {
        if (mAvatarImage != null && bitmap != null) {
            mAvatarImage.setImageBitmap(bitmap);
        }
    }

    private void parseArguments() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mAvatarUrl = arguments.getString(SHOW_FRIEND_AVATAR_IMAGE);
        } else {
            mAvatarUrl = null;
        }
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

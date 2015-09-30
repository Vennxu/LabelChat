package com.ekuater.labelchat.ui.fragment.image;

import android.app.ActionBar;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;

import java.io.File;

/**
 * @author LinYong
 */
public class AvatarUploadFragment extends Fragment {

    private static final String PROGRESS_DIALOG_TAG = "ProgressDialog";

    private static final String ARGS_AVATAR_URI = "args_avatar_uri";

    public static AvatarUploadFragment newInstance(Uri uri,
                                                   OnAvatarUploadListener listener) {
        AvatarUploadFragment instance = new AvatarUploadFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARGS_AVATAR_URI, uri);
        instance.setArguments(args);
        instance.setOnUploadListener(listener);
        return instance;
    }

    private OnAvatarUploadListener mListener;
    private Uri mAvatarUri;
    private SimpleProgressDialog mProgressDialog;
    private AvatarManager mAvatarManager;
    private AvatarManager.UploadListener mUploadListener = new AvatarManager.UploadListener() {
        @Override
        public void onUploadFailed(String userId, AvatarManager.UploadFailType uploadFailType) {
            notifyUploadFailureInUi();
        }

        @Override
        public void onUploadComplete(String userId) {
            notifyUploadSuccessInUi();
        }

        @Override
        public void onUploadProgress(String userId, long bytesWritten, long totalSize) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();

        if (actionBar != null) {
            actionBar.setTitle(R.string.upload_avatar);
        }

        Bundle args = getArguments();
        mAvatarUri = (args != null) ? args.<Uri>getParcelable(ARGS_AVATAR_URI) : null;
        mAvatarManager = AvatarManager.getInstance(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_avatar, container, false);
        ImageView avatarImage = (ImageView) view.findViewById(R.id.avatar_image);
        avatarImage.setImageURI(mAvatarUri);
        startUpload();
        return view;
    }

    public void setOnUploadListener(OnAvatarUploadListener listener) {
        mListener = listener;
    }

    private void runOnUiThread(Runnable task) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(task);
        }
    }

    private void showProgressDialog() {
        dismissProgressDialog();
        mProgressDialog = SimpleProgressDialog.newInstance();
        mProgressDialog.show(getFragmentManager(), PROGRESS_DIALOG_TAG);
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void startUpload() {
        showProgressDialog();
        if (mAvatarUri != null && mAvatarUri.getScheme().equals("file")) {
            mAvatarManager.uploadAvatar(new File(mAvatarUri.getPath()), mUploadListener);
        } else {
            notifyUploadFailureInUi();
        }
    }

    private void notifyUploadSuccessInUi() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissProgressDialog();
                notifyUploadSuccess();
            }
        });
    }

    private void notifyUploadFailureInUi() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissProgressDialog();
                notifyUploadFailure();
            }
        });
    }

    private void notifyUploadSuccess() {
        if (mListener != null) {
            mListener.onUploadSuccess();
        }
    }

    private void notifyUploadFailure() {
        if (mListener != null) {
            mListener.onUploadFailure();
        }
    }
}

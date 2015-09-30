package com.ekuater.labelchat.ui.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;

import com.ekuater.labelchat.EnvConfig;
import com.ekuater.labelchat.R;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.ui.fragment.image.AvatarUploadFragment;
import com.ekuater.labelchat.ui.fragment.image.ImageSelectFragment;
import com.ekuater.labelchat.ui.fragment.image.OnAvatarUploadListener;
import com.ekuater.labelchat.ui.fragment.image.ImageSelectListener;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.util.L;

import java.io.File;

/**
 * @author LinYong
 */
public class AvatarUploadActivity extends BackIconActivity {

    public static final String EXTRA_SAVE_CROPPED_IMAGE = "save_cropped_image";

    private static final String TAG = AvatarUploadActivity.class.getSimpleName();
    private static final String ACTION_IMAGE_CROP = "com.android.camera.action.CROP";
    private static final int REQUEST_CROP_AVATAR = 101;

    private Uri mCroppingImageUri;
    private boolean mIsCroppingImageTemp;
    private Uri mCroppedImageUri;
    private boolean mSaveCroppedImage;
    private boolean mKeepCroppedImage;

    private final ImageSelectListener mImageSelectListener
            = new ImageSelectListener() {
        @Override
        public void onSelectSuccess(String imagePath, boolean isTemp) {
            Uri uri = Uri.fromFile(new File(imagePath));
            mCroppingImageUri = uri;
            mIsCroppingImageTemp = isTemp;
            showCropAvatar(uri);
        }

        @Override
        public void onMultiSelectSuccess(String[] imagePaths) {
        }

        @Override
        public void onSelectFailure() {
            onSelectImageFailure();
        }
    };
    private final OnAvatarUploadListener mAvatarUploadListener
            = new OnAvatarUploadListener() {
        @Override
        public void onUploadSuccess() {
            onAvatarUploadSuccess();
        }

        @Override
        public void onUploadFailure() {
            onAvatarUploadFailure();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mSaveCroppedImage = getIntent().getBooleanExtra(EXTRA_SAVE_CROPPED_IMAGE, false);
        mKeepCroppedImage = false;
        showImageSelectFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        L.d(TAG, "onActivityResult(), requestCode=%1$d,resultCode=%2$d,data=%3$s",
                requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CROP_AVATAR:
                onAvatarCropped(resultCode);
                break;
            default:
                break;
        }
    }

    private void showImageSelectFragment() {
        ImageSelectFragment fragment = ImageSelectFragment.newInstance(
                getString(R.string.select_avatar), mImageSelectListener);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment,
                ImageSelectFragment.class.getSimpleName());
        transaction.commitAllowingStateLoss();
    }

    private void showCropAvatar(Uri uri) {
        Intent intent = new Intent(ACTION_IMAGE_CROP);
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 600);
        intent.putExtra("outputY", 600);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("noFaceDetection", false);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        mCroppedImageUri = Uri.fromFile(EnvConfig.genTempFile(".jpg"));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCroppedImageUri);
        startActivityForResult(intent, REQUEST_CROP_AVATAR);
    }

    private void onAvatarCropped(int resultCode) {
        if (resultCode == RESULT_OK) {
            showUploadFragment(mCroppedImageUri);
        } else {
            ShowToast.makeText(this, R.drawable.emoji_cry,
                    getString(R.string.crop_avatar_failed)).show();
            finish();
        }
        cleanSelectImage();
    }

    private void showUploadFragment(Uri uri) {
        AvatarUploadFragment fragment = AvatarUploadFragment.newInstance(uri,
                mAvatarUploadListener);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment,
                ImageSelectFragment.class.getSimpleName());
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commitAllowingStateLoss();
    }

    private void cleanSelectImage() {
        L.d(TAG, "cleanSelectImage()");
        if (mIsCroppingImageTemp && mCroppingImageUri != null
                && mCroppingImageUri.getScheme().equals("file")) {
            final File file = new File(mCroppingImageUri.getPath());
            if (file.exists() && file.delete()) {
                L.d(TAG, "cleanSelectImage(), delete file:" + file);
            }
        }
    }

    private void cleanCroppedImage() {
        if (mKeepCroppedImage) {
            // keep this cropped image
            return;
        }

        L.d(TAG, "cleanCroppedImage()");
        if (mCroppedImageUri != null && mCroppedImageUri.getScheme().equals("file")) {
            final File file = new File(mCroppedImageUri.getPath());
            if (file.exists() && file.delete()) {
                L.d(TAG, "cleanCroppedImage(), delete file:" + file);
            }
        }
    }

    private void onSelectImageFailure() {
        ShowToast.makeText(this, R.drawable.emoji_cry,
                getString(R.string.select_image_failed)).show();
        finish();
    }

    private void onAvatarUploadSuccess() {
        ShowToast.makeText(this, R.drawable.emoji_smile,
                getString(R.string.upload_avatar_success)).show();
        setCroppedResult();
        finish();
        cleanCroppedImage();
    }

    private void onAvatarUploadFailure() {
        ShowToast.makeText(this, R.drawable.emoji_cry,
                getString(R.string.upload_avatar_failure)).show();
        finish();
        cleanCroppedImage();
    }

    private void setCroppedResult() {
        if (mSaveCroppedImage) {
            Intent intent = new Intent();
            intent.setData(mCroppedImageUri);
            setResult(RESULT_OK, intent);
            mKeepCroppedImage = true;
        }
    }
}

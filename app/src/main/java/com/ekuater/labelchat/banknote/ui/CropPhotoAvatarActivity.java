package com.ekuater.labelchat.banknote.ui;

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
import com.ekuater.labelchat.ui.fragment.image.ImageSelectFragment;
import com.ekuater.labelchat.ui.fragment.image.ImageSelectListener;
import com.ekuater.labelchat.util.L;

import java.io.File;

/**
 * Created by Leo on 2015/5/16.
 *
 * @author LinYong
 */
public class CropPhotoAvatarActivity extends BackIconActivity {

    private static final String TAG = CropPhotoAvatarActivity.class.getSimpleName();

    private static final String ACTION_IMAGE_CROP = "com.android.camera.action.CROP";

    private static final int REQUEST_CROP_AVATAR = 11;

    private final ImageSelectListener mImageSelectListener
            = new ImageSelectListener() {
        @Override
        public void onSelectSuccess(String imagePath, boolean isTemp) {
            onPhotoSelectSuccess(imagePath, isTemp);
        }

        @Override
        public void onMultiSelectSuccess(String[] imagePaths) {
            finish();
        }

        @Override
        public void onSelectFailure() {
            onPhotoSelectFailure();
        }
    };

    private String mTempPhotoFile;
    private Uri mCroppedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.activity_fragment_container);
        showImageSelectFragment();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteTempPhotoFile();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CROP_AVATAR:
                onCropAvatarResult(resultCode, data);
                break;
            default:
                break;
        }
    }

    private void showImageSelectFragment() {
        ImageSelectFragment fragment = ImageSelectFragment.newInstance(
                getString(R.string.banknote_select_photo_title),
                mImageSelectListener);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment,
                ImageSelectFragment.class.getSimpleName());
        transaction.commitAllowingStateLoss();
    }

    private void onPhotoSelectSuccess(String photoPath, boolean isTemp) {
        L.v(TAG, "onPhotoSelectSuccess(), photoPath=%1$s, isTemp=%2$s", photoPath, isTemp);
        deleteTempPhotoFile();
        deleteCroppedImage();
        mTempPhotoFile = isTemp ? photoPath : null;
        showCropAvatar(Uri.fromFile(new File(photoPath)));
    }

    private void onPhotoSelectFailure() {
        L.v(TAG, "onPhotoSelectFailure()");
    }

    private void showCropAvatar(Uri uri) {
        Intent intent = new Intent(ACTION_IMAGE_CROP);
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("noFaceDetection", false);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        mCroppedImageUri = Uri.fromFile(EnvConfig.genTempFile(".jpg"));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCroppedImageUri);
        startActivityForResult(intent, REQUEST_CROP_AVATAR);
    }

    private void onCropAvatarResult(int resultCode, Intent data) {
        deleteTempPhotoFile();
        if (RESULT_OK == resultCode && data != null) {
            Intent intent = new Intent();
            intent.setData(mCroppedImageUri);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            deleteCroppedImage();
        }
    }

    private void deleteCroppedImage() {
        if (mCroppedImageUri != null) {
            if (mCroppedImageUri.getScheme().equals("file")) {
                //noinspection ResultOfMethodCallIgnored
                new File(mCroppedImageUri.getPath()).delete();
            }
            mCroppedImageUri = null;
        }
    }

    private void deleteTempPhotoFile() {
        if (mTempPhotoFile != null) {
            //noinspection ResultOfMethodCallIgnored
            new File(mTempPhotoFile).delete();
            mTempPhotoFile = null;
        }
    }
}

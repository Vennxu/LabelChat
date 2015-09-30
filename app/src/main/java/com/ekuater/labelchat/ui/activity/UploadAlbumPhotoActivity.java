package com.ekuater.labelchat.ui.activity;

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
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.util.L;

import java.io.File;

/**
 * Created by Leo on 2015/3/21.
 *
 * @author LinYong
 */
public class UploadAlbumPhotoActivity extends BackIconActivity {

    private static final String TAG = UploadAlbumPhotoActivity.class.getSimpleName();
    private static final String ACTION_IMAGE_CROP = "com.android.camera.action.CROP";
    private static final int REQUEST_CROP_AVATAR = 101;

    private Uri mCroppingImageUri;
    private boolean mIsCroppingImageTemp;
    private Uri mCroppedImageUri;

    private final ImageSelectListener mImageSelectListener
            = new ImageSelectListener() {
        @Override
        public void onSelectSuccess(String imagePath, boolean isTemp) {
            Uri uri = Uri.fromFile(new File(imagePath));
            mCroppingImageUri = uri;
            mIsCroppingImageTemp = isTemp;
            showCropPhoto(uri);
        }

        @Override
        public void onMultiSelectSuccess(String[] imagePaths) {
        }

        @Override
        public void onSelectFailure() {
            onSelectImageFailure();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);
        showImageSelectFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        L.d(TAG, "onActivityResult(), requestCode=%1$d,resultCode=%2$d,data=%3$s",
                requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CROP_AVATAR:
                onPhotoCropped(resultCode);
                break;
            default:
                break;
        }
    }

    private void showImageSelectFragment() {
        ImageSelectFragment fragment = ImageSelectFragment.newInstance(
                getString(R.string.select_photo), mImageSelectListener);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment,
                ImageSelectFragment.class.getSimpleName());
        transaction.commitAllowingStateLoss();
    }

    private void onSelectImageFailure() {
        ShowToast.makeText(this, R.drawable.emoji_cry,
                getString(R.string.select_image_failed)).show();
        finish();
    }

    private void showCropPhoto(Uri uri) {
        Intent intent = new Intent(ACTION_IMAGE_CROP);
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 1500);
        intent.putExtra("outputY", 1500);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

        File imageFile = EnvConfig.genTempFile(".jpg");
        if (imageFile != null) {
            mCroppedImageUri = Uri.fromFile(EnvConfig.genTempFile(".jpg"));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mCroppedImageUri);
            startActivityForResult(intent, REQUEST_CROP_AVATAR);
        } else {
            mCroppedImageUri = null;
        }
    }

    private void onPhotoCropped(int resultCode) {
        if (resultCode == RESULT_OK) {
            setCroppedResult();
        } else {
            ShowToast.makeText(this, R.drawable.emoji_cry,
                    getString(R.string.crop_picture_failed)).show();
        }
        finish();
        cleanSelectImage();
    }

    private void setCroppedResult() {
        Intent intent = new Intent();
        intent.setData(mCroppedImageUri);
        setResult(RESULT_OK, intent);
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
}

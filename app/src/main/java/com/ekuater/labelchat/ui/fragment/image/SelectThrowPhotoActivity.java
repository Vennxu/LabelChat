package com.ekuater.labelchat.ui.fragment.image;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.util.L;

/**
 * @author LinYong
 */
public class SelectThrowPhotoActivity extends BackIconActivity {

    private static final String TAG = SelectThrowPhotoActivity.class.getSimpleName();

    public static final String EXTRA_FILE_PATH = "file_path";
    public static final String EXTRA_IS_TEMP = "isTemp";

    private final ImageSelectListener mSelectListener = new ImageSelectListener() {

        @Override
        public void onSelectSuccess(String imagePath, boolean isTemp) {
            onImageSelectSuccess(new String[]{imagePath,}, isTemp);
        }

        @Override
        public void onMultiSelectSuccess(String[] imagePaths) {
            onImageSelectSuccess(imagePaths, false);
        }

        @Override
        public void onSelectFailure() {
            onImageSelectFailure();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);
        showImageSelectFragment();
    }

    private void showImageSelectFragment() {
        SelectThrowPhotoFragment fragment = SelectThrowPhotoFragment.newInstance(
                null, 3, mSelectListener);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment,
                SelectThrowPhotoFragment.class.getSimpleName());
        transaction.commitAllowingStateLoss();
    }

    private void onImageSelectSuccess(String[] imagePaths, boolean isTemp) {
        for (String path : imagePaths) {
            L.v(TAG, "onImageSelectSuccess(), path=" + path);
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_FILE_PATH, imagePaths);
        intent.putExtra(EXTRA_IS_TEMP, isTemp);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void onImageSelectFailure() {
        L.v(TAG, "onImageSelectFailure()");
        finish();
    }
}

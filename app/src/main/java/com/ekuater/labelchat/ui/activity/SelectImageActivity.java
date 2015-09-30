package com.ekuater.labelchat.ui.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.ui.fragment.image.ImageSelectFragment;
import com.ekuater.labelchat.ui.fragment.image.ImageSelectListener;
import com.ekuater.labelchat.util.L;

import java.io.File;

/**
 * @author LinYong
 */
public class SelectImageActivity extends BackIconActivity {

    public static final String EXTRA_FILE_PATH = "file_path";
    public static final String EXTRA_IS_TEMP = "isTemp";

    private static final String TAG = SelectImageActivity.class.getSimpleName();

    private final ImageSelectListener mImageSelectListener
            = new ImageSelectListener() {
        @Override
        public void onSelectSuccess(String imagePath, boolean isTemp) {
            notifySelectSuccess(imagePath, isTemp);
        }

        @Override
        public void onMultiSelectSuccess(String[] imagePaths) {
            finish();
        }

        @Override
        public void onSelectFailure() {
            notifySelectFailure();
        }
    };

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

    private void showImageSelectFragment() {
        ImageSelectFragment fragment = ImageSelectFragment.newInstance(mImageSelectListener);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment,
                ImageSelectFragment.class.getSimpleName());
        transaction.commitAllowingStateLoss();
    }

    private void notifySelectSuccess(String imagePath, boolean isTemp) {
        L.v(TAG, "notifySelectSuccess(), imagePath=%1$s, isTemp=%2$s", imagePath, isTemp);
        Intent intent = new Intent();
        Uri uri = Uri.fromFile(new File(imagePath));
        intent.setData(uri);
        intent.putExtra(EXTRA_IS_TEMP, isTemp);
        if (uri.getScheme().equals("file")) {
            intent.putExtra(EXTRA_FILE_PATH, uri.getPath());
        } else {
            intent.putExtra(EXTRA_FILE_PATH, "");
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    private void notifySelectFailure() {
        L.v(TAG, "notifySelectFailure()");
        finish();
    }
}

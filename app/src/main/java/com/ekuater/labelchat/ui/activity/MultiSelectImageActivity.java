package com.ekuater.labelchat.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.ui.fragment.image.ImageSelectListener;
import com.ekuater.labelchat.ui.fragment.image.MultiSelectImageFragment;
import com.ekuater.labelchat.ui.fragment.labelstory.SendLabelStoryFragment;
import com.ekuater.labelchat.util.L;

import java.util.ArrayList;

/**
 * @author LinYong
 */
public class MultiSelectImageActivity extends BackIconActivity {

    private static final String TAG = MultiSelectImageActivity.class.getSimpleName();

    public static final String EXTRA_FILE_PATH = "file_path";
    public static final String EXTRA_IS_TEMP = "isTemp";
    public static final String SELECT_IMAGE_PAGER = "select_image_pager";
    public static final String ACTIONBAR_TITLE = "actionbar_title";
    public static final String SELECT_BUTTON_TEXT = "select_button_text";
    public static final String IMAGE_URLS = "image_urls";
    private int mSelctPager;
    private String selectTitle;
    private String mSelectButtonText;
    private ArrayList<String> mImageUrls;


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
        paramArgment();
        showImageSelectFragment();
    }

    public void paramArgment() {
        Intent intent = getIntent();
        mSelctPager = intent.getIntExtra(SELECT_IMAGE_PAGER, 0);
        selectTitle = intent.getStringExtra(ACTIONBAR_TITLE);
        mSelectButtonText = intent.getStringExtra(SELECT_BUTTON_TEXT);
        mImageUrls = intent.getStringArrayListExtra(IMAGE_URLS);
    }

    private void showImageSelectFragment() {
        MultiSelectImageFragment fragment = MultiSelectImageFragment.newInstance(
                selectTitle, mSelectButtonText, mImageUrls, mSelctPager, mSelectListener);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment,
                MultiSelectImageFragment.class.getSimpleName());
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

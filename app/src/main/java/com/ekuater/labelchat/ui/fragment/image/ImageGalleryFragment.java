package com.ekuater.labelchat.ui.fragment.image;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.AlbumPhoto;
import com.ekuater.labelchat.ui.fragment.SimpleProgressHelper;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ShowToast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2015/5/22.
 *
 * @author LinYong
 */
public class ImageGalleryFragment extends Fragment implements Handler.Callback,
        ViewPager.OnPageChangeListener, View.OnClickListener {

    public static final String IMAGES = "images";
    public static final String POSITION = "position";

    private Handler mHandler;
    private PhotoPagerAdapter mPagerAdapter;
    private SimpleProgressHelper mProgressHelper;
    private TextView mIdxTextView;
    private ImageView mKeepImage;
    private ViewPager mViewPager;
    private String[] images;
    private int position;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        ActionBar actionBar = activity.getActionBar();

        mHandler = new Handler(this);
        mPagerAdapter = new PhotoPagerAdapter(getChildFragmentManager());
        mProgressHelper = new SimpleProgressHelper(this);
        if (actionBar != null) {
            actionBar.hide();
        }
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        parseArguments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image_gallery, container, false);
        mIdxTextView = (TextView) rootView.findViewById(R.id.title);
        mKeepImage = (ImageView) rootView.findViewById(R.id.keep_image);
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(this);
        mKeepImage.setOnClickListener(this);
        mPagerAdapter.updateAlbumPhotos(images);
        updateIdxText();
        mViewPager.setCurrentItem(position);
        return rootView;
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        return handled;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        updateIdxText();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.keep_image:
                keepImage();
                break;
            default:
                break;
        }
    }

    private void keepImage(){
        if (getCurrentPhoto() == null){
            ShowToast.makeText(getActivity(), R.drawable.emoji_cry, getActivity().getResources().getString(R.string.saved_failed)).show();
            return;
        }
        String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), getCurrentPhoto(), null, null);
        ShowToast.makeText(getActivity(), R.drawable.emoji_smile, getActivity().getResources().getString(R.string.saved) + path).show();
    }

    private void updateIdxText() {
        int count = mViewPager.getAdapter().getCount();
        int idx = (count > 0) ? mViewPager.getCurrentItem() + 1 : 0;
        mIdxTextView.setText(getString(R.string.photo_index, idx, count));
    }

    private void finish() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    private void parseArguments() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            images = bundle.getStringArray(IMAGES);
            position = bundle.getInt(POSITION);
        }
    }


    private Bitmap getCurrentPhoto() {
        int idx = mViewPager.getCurrentItem();
        int count = mPagerAdapter.getCount();
        ImageDisplayFragment fragment = (0 <= idx && idx < count)
                ? mPagerAdapter.getItem(idx) : null;
        return (fragment != null) ? fragment.getImageBitmap() : null;
    }

    private static class PhotoPagerAdapter extends FragmentPagerAdapter {

        private List<ImageDisplayFragment> mPageList;

        public PhotoPagerAdapter(FragmentManager fm) {
            super(fm);
            mPageList = setupPages(null);
        }

        public void updateAlbumPhotos(String[] images) {
            mPageList = setupPages(images);
            notifyDataSetChanged();
        }

        private List<ImageDisplayFragment> setupPages(String[] images) {
            List<ImageDisplayFragment> pageList = new ArrayList<>();

            if (images != null) {
                for (String image : images) {
                    pageList.add(newPage(image));
                }
            }
            return pageList;
        }

        private ImageDisplayFragment newPage(String image) {
            ImageDisplayFragment page = new ImageDisplayFragment();
            page.setAlbumPhoto(image);
            return page;
        }

        @Override
        public ImageDisplayFragment getItem(int position) {
            return mPageList.get(position);
        }

        @Override
        public int getCount() {
            return mPageList.size();
        }
    }
}


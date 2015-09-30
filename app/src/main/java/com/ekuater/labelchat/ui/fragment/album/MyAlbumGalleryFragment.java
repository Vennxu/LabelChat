package com.ekuater.labelchat.ui.fragment.album;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.AlbumPhoto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2015/3/23.
 *
 * @author LinYong
 */
public class MyAlbumGalleryFragment extends Fragment {

    public static final String EXTRA_ALBUM_PHOTOS = "extra_album_photos";
    public static final String EXTRA_DEFAULT_ITEM = "extra_default_item";

    private PhotoPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;
    private TextView mIdxTextView;

    private final ViewPager.OnPageChangeListener mOnPageChangeListener
            = new ViewPager.OnPageChangeListener() {

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
    };

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
        parseArguments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_album_gallery, container, false);
        mIdxTextView = (TextView) rootView.findViewById(R.id.title);
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(mPagerAdapter.getDefaultItem());
        mViewPager.setOnPageChangeListener(mOnPageChangeListener);
        updateIdxText();
        return rootView;
    }

    private void updateIdxText() {
        int count = mViewPager.getAdapter().getCount();
        int idx = (count > 0) ? mViewPager.getCurrentItem() + 1 : 0;
        mIdxTextView.setText(getString(R.string.photo_index, idx, count));
    }

    private void parseArguments() {
        Bundle args = getArguments();
        Parcelable[] parcelables = (args != null) ? args.getParcelableArray(
                EXTRA_ALBUM_PHOTOS) : null;
        int item = (args != null) ? args.getInt(EXTRA_DEFAULT_ITEM, 0) : 0;
        AlbumPhoto[] photos;

        if (parcelables != null) {
            photos = new AlbumPhoto[parcelables.length];
            for (int i = 0; i < parcelables.length; ++i) {
                photos[i] = (AlbumPhoto) parcelables[i];
            }
        } else {
            photos = null;
        }
        mPagerAdapter = new PhotoPagerAdapter(getChildFragmentManager(), photos);
        mPagerAdapter.setDefaultItem(item);
    }

    private static class PhotoPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> mPageList;
        private int mDefaultItem;

        public PhotoPagerAdapter(FragmentManager fm, AlbumPhoto[] photos) {
            super(fm);
            mPageList = setupPages(photos);
        }

        public int getDefaultItem() {
            return mDefaultItem;
        }

        public void setDefaultItem(int item) {
            mDefaultItem = item;
        }

        private List<Fragment> setupPages(AlbumPhoto[] photos) {
            List<Fragment> pageList = new ArrayList<>();

            if (photos != null) {
                for (AlbumPhoto photo : photos) {
                    pageList.add(newPage(photo));
                }
            }
            return pageList;
        }

        private Fragment newPage(AlbumPhoto photo) {
            Fragment page = new MyPhotoDisplayFragment();
            Bundle args = new Bundle();
            args.putParcelable(MyPhotoDisplayFragment.EXTRA_ALBUM_PHOTO, photo);
            page.setArguments(args);
            return page;
        }

        @Override
        public Fragment getItem(int position) {
            return mPageList.get(position);
        }

        @Override
        public int getCount() {
            return mPageList.size();
        }
    }
}

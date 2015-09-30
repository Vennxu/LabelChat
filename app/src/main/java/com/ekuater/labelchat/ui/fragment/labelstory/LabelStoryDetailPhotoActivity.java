package com.ekuater.labelchat.ui.fragment.labelstory;


import java.util.ArrayList;
import java.util.List;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ekuater.labelchat.R;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class LabelStoryDetailPhotoActivity extends Activity {

    private ArrayList<View> listViews = null;
    private ViewPager pager;
    private MyPageAdapter adapter;
    private int count;
    public int max;
    public int selected;
    public ArrayList<String> mArrayList = new ArrayList<String>();
    RelativeLayout photo_relativeLayout;

    public View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.photo_bt_del:
                    if (listViews.size() == 1) {
                        mArrayList.clear();
                        putParam();
                    } else {
                        pager.removeAllViews();
                        listViews.remove(count);
                        mArrayList.remove(count);
                        adapter.setListViews(listViews);
                        adapter.notifyDataSetChanged();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_labelstory_detailphoto);
        paramArgment();
        photo_relativeLayout = (RelativeLayout) findViewById(R.id.photo_relativeLayout);
        photo_relativeLayout.setBackgroundColor(0x70000000);
        findViewById(R.id.photo_bt_del).setOnClickListener(mOnClickListener);
        pager = (ViewPager) findViewById(R.id.viewpager);
        pager.setOnPageChangeListener(pageChangeListener);
        for (int i = 0; i < mArrayList.size(); i++) {
            initListViews(mArrayList.get(i));
        }
        adapter = new MyPageAdapter(listViews);
        pager.setAdapter(adapter);
        pager.setCurrentItem(selected);
    }


    private void paramArgment() {
        Intent intent = getIntent();
        mArrayList.addAll(intent.getStringArrayListExtra(SendLabelStoryFragment.DETAIL_IMAGE_LIST));
        selected = intent.getIntExtra(SendLabelStoryFragment.DETAIL_IMAGE_SELECTED, 0);
        Log.i("mArrayListDetail", mArrayList.size() + "--- " + selected);
    }

    private void initListViews(String strUrl) {
        if (listViews == null)
            listViews = new ArrayList<View>();
        View rootView = getLayoutInflater().inflate(R.layout.fragment_photo_display, null, false);
        PhotoViewListener listener = new PhotoViewListener();
        PhotoView img = (PhotoView) rootView.findViewById(R.id.photo);
        img.setOnViewTapListener(listener);
        img.setOnPhotoTapListener(listener);
        img.setBackgroundColor(0xff000000);
        img.setImageBitmap(SendLabelStoryFragment.readBitmap(strUrl));
        listViews.add(rootView);// 添加view
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isBack = false;
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            putParam();
        }
        return isBack;
    }

    private void putParam(){
        Intent intent = new Intent();
        intent.putStringArrayListExtra(SendLabelStoryFragment.DETAIL_IMAGE_LIST, mArrayList);
        setResult(RESULT_OK, intent);
        finish();
    }

    private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

        public void onPageSelected(int arg0) {
            count = arg0;
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        public void onPageScrollStateChanged(int arg0) {// 滑动状态改变

        }
    };

    private class PhotoViewListener implements PhotoViewAttacher.OnViewTapListener,
            PhotoViewAttacher.OnPhotoTapListener {

        @Override
        public void onPhotoTap(View view, float x, float y) {
            putParam();
        }

        @Override
        public void onViewTap(View view, float x, float y) {
            putParam();
        }
    }

    class MyPageAdapter extends PagerAdapter {

        private ArrayList<View> listViews;// content

        private int size;// 页数

        public MyPageAdapter(ArrayList<View> listViews) {

            this.listViews = listViews;
            size = listViews == null ? 0 : listViews.size();
        }

        public void setListViews(ArrayList<View> listViews) {
            this.listViews = listViews;
            size = listViews == null ? 0 : listViews.size();
        }

        public int getCount() {// 返回数量
            return size;
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(listViews.get(arg1 % size));
        }

        public void finishUpdate(View arg0) {
        }

        public Object instantiateItem(View arg0, int arg1) {
            try {
                ((ViewPager) arg0).addView(listViews.get(arg1 % size), 0);

            } catch (Exception e) {
            }
            return listViews.get(arg1 % size);
        }

        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

    }
}

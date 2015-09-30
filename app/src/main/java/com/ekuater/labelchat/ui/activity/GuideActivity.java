package com.ekuater.labelchat.ui.activity;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.activity.base.TitleIconActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2014/12/24.
 *
 * @author LinYong
 */
public class GuideActivity extends TitleIconActivity
        implements ViewPager.OnPageChangeListener, View.OnClickListener {

    private View[] mDotViews;
    private int mCurrentIndex;
    private Button mContinueBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        initViews();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onContinueClick();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        setCurrentDot(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_continue:
                onContinueClick();
                break;
            default:
                break;
        }
    }

    private List<View> setupGuidePages(ViewGroup parent) {
        final TypedArray ar = getResources().obtainTypedArray(R.array.guide_images);
        final LayoutInflater inflater = LayoutInflater.from(this);
        final int length = ar.length();
        final List<View> pageList = new ArrayList<>();

        for (int i = 0; i < length; ++i) {
            final int resId = ar.getResourceId(i, 0);
            if (resId != 0) {
                View view = inflater.inflate(R.layout.guide_item, parent, false);
                view.setBackgroundResource(resId);
                pageList.add(view);
            }
        }
        ar.recycle();
        return pageList;
    }

    private View[] setupDots(ViewGroup parent, int count) {
        final LayoutInflater inflater = LayoutInflater.from(this);
        final View[] dotViews = new View[count];

        for (int i = 0; i < count; ++i) {
            View view = inflater.inflate(R.layout.guide_dot_view, parent, false);
            view.setSelected(false);
            parent.addView(view);
            dotViews[i] = view;
        }
        return dotViews;
    }

    private void initViews() {
        final ViewPager viewPager = (ViewPager) findViewById(R.id.guide_pager);
        final ViewGroup dotsGroup = (ViewGroup) findViewById(R.id.dots_group);
        final List<View> pageList = setupGuidePages(viewPager);
        final int count = pageList.size();
        final GuidePageAdapter adapter = new GuidePageAdapter(pageList);

        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(this);
        mContinueBtn = (Button) findViewById(R.id.btn_continue);
        mContinueBtn.setOnClickListener(this);
        // initialize dots
        mDotViews = setupDots(dotsGroup, count);
        mCurrentIndex = 0;
        setCurrentDot(0);
    }

    private void setCurrentDot(int position) {
        final int length = mDotViews.length;

        if (position < 0 || position > (length - 1)) {
            return;
        }

        mDotViews[mCurrentIndex].setSelected(false);
        mDotViews[position].setSelected(true);
        mCurrentIndex = position;
        mContinueBtn.setText(position == (length - 1) ? R.string.start : R.string.skip);
    }

    private boolean hasLogin() {
        AccountManager accountManager = AccountManager.getInstance(this);
        return (accountManager.isLogin() || accountManager.isAutoLogin());
    }

    private void onContinueClick() {
        if (hasLogin()) {
            goMainUI();
        } else {
            goSignInUI();
        }
    }

    private void goSignInUI() {
        UILauncher.launchSignInGuideUI(this);
        finish();
    }

    private void goMainUI() {
        UILauncher.launchMainUI(this);
        finish();
    }

    private static class GuidePageAdapter extends PagerAdapter {

        private final List<View> mPageList;

        public GuidePageAdapter(List<View> viewList) {
            super();
            mPageList = viewList;
        }

        @Override
        public int getCount() {
            return mPageList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mPageList.get(position);
            container.addView(view, 0);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mPageList.get(position));
        }
    }
}

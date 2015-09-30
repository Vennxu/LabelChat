package com.ekuater.labelchat.ui.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ekuater.labelchat.R;

/**
 * @author LinYong
 */
public class PageGuider implements ViewPager.OnPageChangeListener, View.OnClickListener {

    public interface Listener {

        public void onShow();

        public void onHide();
    }

    private interface ListenerNotifier {

        public void notify(Listener listener);
    }

    private final Context mContext;
    private final FrameLayout mParent;
    private final int[] mImageIds;

    private Listener mListener;
    private ViewHolder mViewHolder;

    public PageGuider(Context context, FrameLayout parent, int imageArrayId) {
        if (context == null) {
            throw new NullPointerException("PageGuider empty context");
        }
        if (parent == null) {
            throw new NullPointerException("PageGuider empty guide parent view");
        }
        if (imageArrayId == 0) {
            throw new NullPointerException("PageGuider empty guide image array");
        }

        mContext = context;
        mParent = parent;

        final TypedArray ar = mContext.getResources().obtainTypedArray(imageArrayId);
        final int length = ar.length();

        if (length == 0) {
            throw new NullPointerException("PageGuider empty guide image array");
        }

        mImageIds = new int[length];
        for (int i = 0; i < length; ++i) {
            mImageIds[i] = ar.getResourceId(i, 0);
        }
        ar.recycle();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void showGuide() {
        if (mViewHolder == null) {
            showGuideInternal();
        }
    }

    public void hideGuide() {
        if (mViewHolder != null) {
            hideGuideInternal();
        }
    }

    private void hideGuideInternal() {
        mViewHolder.mGuideView.setVisibility(View.GONE);
        mParent.removeView(mViewHolder.mGuideView);

        notifyListener(new HideNotifier());
    }

    private void showGuideInternal() {
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        final View guideView = inflater.inflate(R.layout.guide_layout, mParent, false);
        final ViewPager viewPager = (ViewPager) guideView.findViewById(R.id.guide_pager);
        final int length = mImageIds.length;
        final View[] pages = new View[length];

        for (int i = 0; i < length; ++i) {
            View page = inflater.inflate(R.layout.guide_item, viewPager, false);
            page.setBackgroundResource(mImageIds[i]);
            pages[i] = page;
        }

        viewPager.setAdapter(new GuidePageAdapter(pages));
        viewPager.setOnPageChangeListener(this);

        View startView = guideView.findViewById(R.id.btn_start);
        startView.setOnClickListener(this);

        // initialize dots
        ViewGroup dotsGroup = (ViewGroup) guideView.findViewById(R.id.dots_group);
        View[] dotViews = new View[length];
        for (int i = 0; i < length; ++i) {
            View dot = inflater.inflate(R.layout.guide_dark_dot_view, dotsGroup, false);
            dot.setSelected(false);
            dotsGroup.addView(dot);
            dotViews[i] = dot;
        }

        mViewHolder = new ViewHolder();
        mViewHolder.mGuideView = guideView;
        mViewHolder.mStartView = startView;
        mViewHolder.mDotViews = dotViews;
        mViewHolder.mCurrentIndex = 0;
        setCurrentDot(0);

        final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        mParent.addView(guideView, lp);

        notifyListener(new ShowNotifier());
    }

    private void setCurrentDot(int position) {
        final int length = mViewHolder.mDotViews.length;

        if (position < 0 || position > (length - 1)) {
            return;
        }

        mViewHolder.mDotViews[mViewHolder.mCurrentIndex].setSelected(false);
        mViewHolder.mDotViews[position].setSelected(true);
        mViewHolder.mCurrentIndex = position;
        mViewHolder.mStartView.setVisibility((position == (length - 1) ? View.VISIBLE : View.GONE));
    }

    private void notifyListener(ListenerNotifier notifier) {
        if (mListener != null) {
            notifier.notify(mListener);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                hideGuideInternal();
                break;
            default:
                break;
        }
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

    private static class ViewHolder {

        public View mGuideView;
        public View mStartView;
        public View[] mDotViews;
        public int mCurrentIndex;
    }

    private static class GuidePageAdapter extends PagerAdapter {

        private final View[] mPages;

        public GuidePageAdapter(View[] pages) {
            super();
            mPages = pages;
        }

        @Override
        public int getCount() {
            return mPages.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return (view == o);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mPages[position];
            container.addView(view, 0);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mPages[position]);
        }
    }

    private static class ShowNotifier implements ListenerNotifier {

        @Override
        public void notify(Listener listener) {
            listener.onShow();
        }
    }

    private static class HideNotifier implements ListenerNotifier {

        @Override
        public void notify(Listener listener) {
            listener.onHide();
        }
    }
}

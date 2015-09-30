package com.ekuater.labelchat.ui.fragment.friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.UserLabel;

/**
 * Created by Leo on 2015/1/27.
 *
 * @author LinYong
 */
/*package*/ class PraiseLabelItem {

    private static final int VIEW_TYPE_LABEL = 0;
    private static final int VIEW_TYPE_RECOMMEND = 1;
    private static final int VIEW_TYPE_COUNT = 2;

    public static int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    public interface Item {

        public View newView(LayoutInflater inflater, ViewGroup parent);

        public void bindView(View view);

        public int getViewType();

        public int getCompareValue();

        public void onClick();

        public boolean onLongClick();
    }

    public interface LabelClickListener {
        public void onClick(UserLabel label);

        public boolean onLongClick(UserLabel label);
    }

    public static class LabelItem implements Item {

        private UserLabel mUserLabel;
        private boolean mMyLabel;
        private LabelClickListener mListener;

        public LabelItem(UserLabel userLabel, boolean myLabel, LabelClickListener listener) {
            mUserLabel = userLabel;
            mMyLabel = myLabel;
            mListener = listener;
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            View view = inflater.inflate(R.layout.praise_label_item, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.labelText = (TextView) view.findViewById(R.id.label);
            holder.praiseText = (TextView) view.findViewById(R.id.praise);
            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.labelText.setText(mUserLabel.getName());
            holder.praiseText.setText(String.valueOf(mUserLabel.getPraiseCount()));
            view.setBackgroundResource(mMyLabel ? R.drawable.praise_label_equal
                    : R.drawable.praise_label_different);
        }

        @Override
        public int getViewType() {
            return VIEW_TYPE_LABEL;
        }

        @Override
        public int getCompareValue() {
            return mUserLabel.getPraiseCount();
        }

        @Override
        public void onClick() {
            if (mListener != null) {
                mListener.onClick(mUserLabel);
            }
        }

        @Override
        public boolean onLongClick() {
            return mListener != null && mListener.onLongClick(mUserLabel);
        }

        private static class ViewHolder {
            public TextView labelText;
            public TextView praiseText;
        }
    }

    public interface RecommendListener {
        public void onRecommend();
    }

    public static class RecommendItem implements Item {

        private final RecommendListener listener;

        public RecommendItem(RecommendListener listener) {
            this.listener = listener;
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            return inflater.inflate(R.layout.label_recommend_item, parent, false);
        }

        @Override
        public void bindView(View view) {
        }

        @Override
        public int getViewType() {
            return VIEW_TYPE_RECOMMEND;
        }

        @Override
        public int getCompareValue() {
            return Integer.MAX_VALUE;
        }

        @Override
        public void onClick() {
            if (listener != null) {
                listener.onRecommend();
            }
        }

        @Override
        public boolean onLongClick() {
            return false;
        }
    }
}

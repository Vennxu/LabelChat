package com.ekuater.labelchat.ui.fragment.usershowpage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ekuater.labelchat.R;

/**
 * Created by Leo on 2015/2/4.
 *
 * @author LinYong
 */
public final class UserInfoItem {

    private static final int VIEW_TYPE_SEPARATOR = 0;
    private static final int VIEW_TYPE_INFO_ITEM = 1;
    private static final int VIEW_TYPE_DELETE_FRIEND = 2;
    private static final int VIEW_TYPE_COUNT = 3;


    public static int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    public static interface InfoItem {

        public View newView(LayoutInflater inflater, ViewGroup parent);

        public void bindView(View view);

        public int getShowViewType();

        public boolean isEnabled();

        public void onClick();
    }

    public static class SeparatorItem implements InfoItem {

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            return inflater.inflate(R.layout.user_info_separate_item, parent, false);
        }

        @Override
        public void bindView(View view) {
        }

        @Override
        public int getShowViewType() {
            return VIEW_TYPE_SEPARATOR;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public void onClick() {
        }
    }

    public static interface NormalItemListener {

        public void onClick(NormalInfoItem infoItem);
    }

    public static class NormalInfoItem implements InfoItem {

        private String mTitle;
        private String mContent;
        private boolean mEnable;
        private NormalItemListener mListener;

        public NormalInfoItem(String title, String content, boolean enable) {
            this(title, content, enable, null);
        }

        public NormalInfoItem(String title, String content, NormalItemListener listener) {
            this(title, content, true, listener);
        }

        public NormalInfoItem(String title, String content, boolean enable,
                              NormalItemListener listener) {
            mTitle = title;
            mContent = content;
            mEnable = enable;
            mListener = listener;
        }

        public String getContent() {
            return mContent;
        }

        public void setContent(String content) {
            mContent = content;
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            View view = inflater.inflate(R.layout.user_info_item, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.title = (TextView) view.findViewById(R.id.title);
            holder.content = (TextView) view.findViewById(R.id.content);
            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.title.setText(mTitle);
            holder.content.setText(mContent);
        }

        @Override
        public int getShowViewType() {
            return VIEW_TYPE_INFO_ITEM;
        }

        @Override
        public boolean isEnabled() {
            return mEnable;
        }

        @Override
        public void onClick() {
            if (mListener != null) {
                mListener.onClick(this);
            }
        }

        private static class ViewHolder {
            private TextView title;
            private TextView content;
        }
    }

    public static interface DeleteFriendListener {
        public void onDelete();
    }

    public static class DeleteFriendItem implements InfoItem {

        private final DeleteFriendListener listener;

        public DeleteFriendItem(DeleteFriendListener listener) {
            this.listener = listener;
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            return inflater.inflate(R.layout.user_info_delete_item, parent, false);
        }

        @Override
        public void bindView(View view) {
        }

        @Override
        public int getShowViewType() {
            return VIEW_TYPE_DELETE_FRIEND;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void onClick() {
            if (listener != null) {
                listener.onDelete();
            }
        }
    }
}

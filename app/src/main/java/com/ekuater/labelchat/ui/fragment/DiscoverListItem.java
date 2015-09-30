package com.ekuater.labelchat.ui.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;

/**
 * Created by wenxiang on 2015/3/3.
 */
public class DiscoverListItem {

    private static final int VIEW_TYPE_FUNCTION = 0;
    private static final int VIEW_TYPE_SEARCH = 1;
    private static final int VIEW_TYPE_NEWUSER = 2;

    private static final int VIEW_TYPE_COUNT = 3;

    public static int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    public interface Item {

        public View newView(LayoutInflater inflater, ViewGroup parent);

        public void bindView(View view);

        public int getViewType();

        public String getSortLetters();

        public void onClick();

    }

    public interface FunctionItemListener {
        public void onClick(String function);
    }

    public static class FunctionItem implements Item {

        private String mTitle;
        private int mIconId;
        private String mFunction;
        private FunctionItemListener mListener;

        public FunctionItem(String title, int icon, String function, FunctionItemListener listener) {
            mTitle = title;
            mIconId = icon;
            mFunction = function;
            mListener = listener;
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            return inflater.inflate(R.layout.discover_function_item, parent, false);
        }

        @Override
        public void bindView(View view) {
            TextView titleView = (TextView) view.findViewById(R.id.title);
            ImageView iconView = (ImageView) view.findViewById(R.id.icon);

            titleView.setText(mTitle);
            iconView.setImageResource(mIconId);
        }

        @Override
        public int getViewType() {
            return VIEW_TYPE_FUNCTION;
        }

        @Override
        public String getSortLetters() {
            return "?";
        }

        @Override
        public void onClick() {
            if (mListener != null) {
                mListener.onClick(mFunction);
            }
        }
    }


    public interface SearchItemListener {
        public void onSearch();
    }

    public static class SearchItem implements Item {

        private SearchItemListener mListener;

        public SearchItem(SearchItemListener listener) {
            mListener = listener;
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            return inflater.inflate(R.layout.contact_search_item, parent, false);
        }

        @Override
        public void bindView(View view) {
        }

        @Override
        public int getViewType() {
            return VIEW_TYPE_SEARCH;
        }

        @Override
        public String getSortLetters() {
            return "?";
        }

        @Override
        public void onClick() {
            if (mListener != null) {
                mListener.onSearch();
            }
        }
    }
}

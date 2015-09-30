package com.ekuater.labelchat.ui.fragment.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.ui.UILauncher;

/**
 * Created by Leo on 2014/12/29.
 *
 * @author LinYong
 */
/*package*/ class LabelShowMenuItem {

    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_ADD_LABEL = 1;
    private static final int TYPE_COUNT = 2;

    public static int getTypeCount() {
        return TYPE_COUNT;
    }

    public interface Item {

        public View newView(LayoutInflater inflater, ViewGroup parent);

        public void bindView(View view);

        public int getViewType();

        public void onClick();
    }

    public interface NormalItemListener {

        public void onClick(NormalItem item);
    }

    public static class NormalItem implements Item {

        private String mTitle;
        private int mIcon;
        private NormalItemListener mListener;

        public NormalItem(String title, int icon, NormalItemListener listener) {
            mTitle = title;
            mIcon = icon;
            mListener = listener;
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            return inflater.inflate(R.layout.label_show_normal_menu, parent, false);
        }

        @Override
        public void bindView(View view) {
            ImageView iconView = (ImageView) view.findViewById(R.id.icon);
            TextView titleView = (TextView) view.findViewById(R.id.title);
            iconView.setImageResource(mIcon);
            titleView.setText(mTitle);
        }

        @Override
        public int getViewType() {
            return TYPE_NORMAL;
        }

        @Override
        public void onClick() {
            if (mListener != null) {
                mListener.onClick(this);
            }
        }
    }

    public static class AddLabelItem implements Item {

        private Context mContext;

        public AddLabelItem(Context context) {
            mContext = context;
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            return inflater.inflate(R.layout.label_show_add_label_menu, parent, false);
        }

        @Override
        public void bindView(View view) {
        }

        @Override
        public int getViewType() {
            return TYPE_ADD_LABEL;
        }

        @Override
        public void onClick() {
            UILauncher.launchAddUserLabelUI(mContext, null);
        }
    }
}

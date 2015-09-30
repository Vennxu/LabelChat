package com.ekuater.labelchat.ui.fragment.personalinfo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;

/**
 * Created by Leo on 2015/4/1.
 *
 * @author LinYong
 */
public class SubItem {

    public interface Listener {
        public void onClick();
    }

    private int mIcon;
    private String mTitle;
    private String mSubTitle;
    private int mRightIcon;
    public Listener mListener;

    public SubItem(int icon, String title, Listener listener) {
        this(icon, title, null, R.drawable.ic_enter, listener);
    }

    public SubItem(int icon, String title, String subTitle,
                   int rightIcon, Listener listener) {
        mIcon = icon;
        mTitle = title;
        mSubTitle = subTitle;
        mRightIcon = rightIcon;
        mListener = listener;
    }

    public View newView(LayoutInflater inflater, ViewGroup parent) {
        View view = inflater.inflate(R.layout.personal_info_sub_item, parent, false);
        ViewHolder holder = new ViewHolder();
        holder.icon = (ImageView) view.findViewById(R.id.icon);
        holder.title = (TextView) view.findViewById(R.id.title);
        holder.subTitle = (TextView) view.findViewById(R.id.sub_title);
        holder.rightIcon = (ImageView) view.findViewById(R.id.right_icon);
        view.setTag(holder);
        return view;
    }

    public void bindView(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.icon.setImageResource(mIcon);
        holder.title.setText(mTitle);
        holder.subTitle.setText(mSubTitle);
        holder.rightIcon.setImageResource(mRightIcon);
    }

    public void onClick() {
        if (mListener != null) {
            mListener.onClick();
        }
    }

    private static class ViewHolder {

        public ImageView icon;
        public TextView title;
        public TextView subTitle;
        public ImageView rightIcon;
    }
}

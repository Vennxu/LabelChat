package com.ekuater.labelchat.ui.fragment.usershowpage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by Leo on 2015/2/4.
 *
 * @author LinYong
 */
public class UserInfoAdapter extends BaseAdapter
        implements AdapterView.OnItemClickListener {

    private final LayoutInflater mInflater;
    private List<UserInfoItem.InfoItem> mItemList;

    public UserInfoAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    public void updateItems(List<UserInfoItem.InfoItem> list) {
        mItemList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return (mItemList != null) ? mItemList.size() : 0;
    }

    @Override
    public UserInfoItem.InfoItem getItem(int position) {
        return (mItemList != null) ? mItemList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return UserInfoItem.getViewTypeCount();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getShowViewType();
    }

    @Override
    public boolean isEnabled(int position) {
        return getItem(position).isEnabled();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserInfoItem.InfoItem infoItem = getItem(position);
        if (convertView == null) {
            convertView = infoItem.newView(mInflater, parent);
        }
        infoItem.bindView(convertView);
        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
        Object item = parent.getAdapter().getItem(position);
        if (item instanceof UserInfoItem.InfoItem) {
            UserInfoItem.InfoItem infoItem = (UserInfoItem.InfoItem) item;
            infoItem.onClick();
        }
    }
}

package com.ekuater.labelchat.ui.fragment.personalinfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by Leo on 2015/3/13.
 *
 * @author LinYong
 */
public class PersonalInfoAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

    private final LayoutInflater mInflater;
    private List<ViewItem> mItemList;

    public PersonalInfoAdapter(Context context, List<ViewItem> itemList) {
        mInflater = LayoutInflater.from(context);
        mItemList = itemList;
    }

    public void updateItems(List<ViewItem> itemList) {
        mItemList = itemList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItemList != null ? mItemList.size() : 0;
    }

    @Override
    public ViewItem getItem(int position) {
        return mItemList != null ? mItemList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewItem item = getItem(position);
        if (convertView == null) {
            convertView = item.newView(mInflater, parent);
        }
        item.bindView(convertView);
        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return ViewType.getTypeCount();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getViewType();
    }

    @Override
    public boolean isEnabled(int position) {
        return getItem(position).isEnabled();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object object = parent.getItemAtPosition(position);
        if (object != null && object instanceof ViewItem) {
            ((ViewItem) object).onClick();
        }
    }
}

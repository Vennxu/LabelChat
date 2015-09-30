package com.ekuater.labelchat.ui.fragment.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2015/3/9.
 *
 * @author LinYong
 */
public class MessageListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<MessageListItem.Item> mMessageItemList;

    public MessageListAdapter(Context context) {
        super();
        mInflater = LayoutInflater.from(context);
        mMessageItemList = new ArrayList<>();
    }

    public synchronized void updateItems(List<MessageListItem.Item> items) {
        if (items != null) {
            mMessageItemList = items;
        } else {
            mMessageItemList.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMessageItemList.size();
    }

    @Override
    public MessageListItem.Item getItem(int position) {
        return mMessageItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return MessageListItem.getViewTypeCount();
    }

    @Override
    public int getItemViewType(int position) {
        return mMessageItemList.get(position).getShowViewType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageListItem.Item item = getItem(position);
        if (convertView == null) {
            convertView = item.newView(mInflater, parent);
        }
        item.bindView(convertView);
        return convertView;
    }
}

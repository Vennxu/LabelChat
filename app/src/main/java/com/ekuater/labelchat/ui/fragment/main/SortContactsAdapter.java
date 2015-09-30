package com.ekuater.labelchat.ui.fragment.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2015/3/9.
 *
 * @author LinYong
 */
public class SortContactsAdapter extends BaseAdapter
        implements SectionIndexer, AdapterView.OnItemClickListener {

    private final LayoutInflater mInflater;
    private List<ContactsListItem.Item> mItemList;

    public SortContactsAdapter(Context context) {
        super();
        mInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mItemList = new ArrayList<>();
    }

    /**
     * Call to update the contacts
     *
     * @param items new contacts
     */
    public synchronized void updateItems(List<ContactsListItem.Item> items) {
        if (items != null) {
            mItemList = items;
        } else {
            mItemList.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItemList.size();
    }

    @Override
    public ContactsListItem.Item getItem(int position) {
        return mItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return ContactsListItem.getViewTypeCount();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getViewType();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ContactsListItem.Item item = getItem(position);

        if (view == null) {
            view = item.newView(mInflater, parent);
        }
        item.bindView(view);

        return view;
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的Char ascii值
     */
    @Override
    public int getSectionForPosition(int position) {
        int ascii = -1;

        if (0 <= position && position < mItemList.size()) {
            ascii = mItemList.get(position).getSortLetters().charAt(0);
        }

        return ascii;
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    @Override
    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = mItemList.get(i).getSortLetters();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
        getItem(position).onClick();
    }
}

package com.ekuater.labelchat.ui.fragment.personalinfo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

import com.ekuater.labelchat.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2015/4/1.
 *
 * @author LinYong
 */
public class ViewItemContainer implements ViewItem {

    private final List<SubItem> mSubItemList;

    public ViewItemContainer() {
        mSubItemList = new ArrayList<>();
    }

    public void addSubItem(SubItem subItem) {
        mSubItemList.add(subItem);
    }

    @Override
    public int getViewType() {
        return Adapter.IGNORE_ITEM_VIEW_TYPE;
    }

    @Override
    public View newView(LayoutInflater inflater, ViewGroup parent) {
        final View view = inflater.inflate(R.layout.personal_info_container_item,
                parent, false);
        final ViewGroup container = (ViewGroup) view.findViewById(R.id.container);
        final int count = mSubItemList.size();

        for (int i = 0; i < count; ++i) {
            SubItem subItem = mSubItemList.get(i);
            View subView = subItem.newView(inflater, container);
            container.addView(subView);
            subItem.bindView(subView);
            subView.setOnClickListener(new SubItemClickListener(subItem));

            if (i < count - 1) {
                addDivider(inflater, container);
            }
        }
        return view;
    }

    private void addDivider(LayoutInflater inflater, ViewGroup container) {
        View divider = inflater.inflate(R.layout.personal_info_sub_item_divider,
                container, false);
        container.addView(divider);
    }

    @Override
    public void bindView(View view) {
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void onClick() {
    }

    private static class SubItemClickListener implements View.OnClickListener {

        private final SubItem mSubItem;

        public SubItemClickListener(SubItem subItem) {
            mSubItem = subItem;
        }

        @Override
        public void onClick(View v) {
            if (mSubItem != null) {
                mSubItem.onClick();
            }
        }
    }
}

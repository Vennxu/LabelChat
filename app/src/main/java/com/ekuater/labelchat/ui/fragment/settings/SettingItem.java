package com.ekuater.labelchat.ui.fragment.settings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.ekuater.labelchat.R;

/**
 * Created by Leo on 2015/2/9.
 *
 * @author LinYong
 */
public class SettingItem {

    private static final int ITEM_TYPE_TEXT = 0;
    private static final int ITEM_TYPE_VOICE = 1;
    private static final int ITEM_TYPE_EXIT = 2;
    private static final int ITEM_TYPE_COUNT = 3;

    public static int getItemTypeCount() {
        return ITEM_TYPE_COUNT;
    }

    public interface Item {

        public View newView(LayoutInflater inflater, ViewGroup parent);

        public void bindView(View view);

        public int getItemType();

        public boolean isEnabled();

        public void onClick();
    }

    public interface ItemClickListener {

        public void onClick(Item item);
    }

    private static void notifyOnClick(Item item, ItemClickListener listener) {
        if (listener != null) {
            listener.onClick(item);
        }
    }

    public static class TextItem implements Item {

        private String title;
        private ItemClickListener listener;
        private TextView titleView;

        public TextItem(String title, ItemClickListener listener) {
            this.title = title;
            this.listener = listener;
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            View view = inflater.inflate(R.layout.setting_text_item, parent, false);
            titleView = (TextView) view.findViewById(R.id.title);
            return view;
        }

        @Override
        public void bindView(View view) {
            titleView.setText(title);
        }

        @Override
        public int getItemType() {
            return ITEM_TYPE_TEXT;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void onClick() {
            notifyOnClick(this, listener);
        }
    }

    public static class ExitItem implements Item {

        private ItemClickListener listener;

        public ExitItem(ItemClickListener listener) {
            this.listener = listener;
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            return inflater.inflate(R.layout.setting_exit_item, parent, false);
        }

        @Override
        public void bindView(View view) {
        }

        @Override
        public int getItemType() {
            return ITEM_TYPE_EXIT;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void onClick() {
            notifyOnClick(this, listener);
        }
    }
}

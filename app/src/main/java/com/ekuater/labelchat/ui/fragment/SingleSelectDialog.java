package com.ekuater.labelchat.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ekuater.labelchat.R;

/**
 * @author LinYong
 */
public class SingleSelectDialog extends DialogFragment
        implements AdapterView.OnItemClickListener {

    private static final String TAG = SingleSelectDialog.class.getSimpleName();

    public interface IListener {
        public void onItemSelected(int position, CharSequence sequence);
    }

    public static final class UiConfig {

        public String title;
        public CharSequence[] textItems;
        public int[] iconItems;
        public boolean iconInLeft;
        public boolean textInCenter;
        public int height;
        public IListener listener;

        public UiConfig() {
            title = null;
            textItems = null;
            iconItems = null;
            iconInLeft = false;
            textInCenter = false;
            height = -1;
            listener = null;
        }
    }

    public static SingleSelectDialog newInstance(UiConfig config) {
        SingleSelectDialog instance = new SingleSelectDialog();
        instance.applyConfig(config);
        return instance;
    }

    private String mTitle;
    private CharSequence[] mTextItems;
    private int[] mIconItems;
    private boolean mIconInLeft;
    private boolean mTextInCenter;
    private IListener mListener;

    private int mHeight;

    public SingleSelectDialog() {
        setStyle(STYLE_NO_TITLE, 0);
    }

    public void applyConfig(UiConfig config) {
        mTitle = config.title;
        mTextItems = config.textItems;
        mIconItems = config.iconItems;
        mIconInLeft = config.iconInLeft;
        mTextInCenter = config.textInCenter;
        mHeight = config.height;
        mListener = config.listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.single_select_item, container, false);
        TextView titleView = (TextView) view.findViewById(R.id.title_header);
        titleView.setText(mTitle);
        ListView listView = (ListView) view.findViewById(R.id.single_item_list);
        listView.setAdapter(new ItemAdapter(getActivity()));
        listView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mHeight > 0) {
            Window window = getDialog().getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.height = mHeight;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mListener != null) {
            mListener.onItemSelected(position, mTextItems[position]);
        }
        dismiss();
    }

    private class ItemAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private int mItemLayout;

        public ItemAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            mItemLayout = mTextInCenter ? R.layout.select_text_center_item
                    : (mIconInLeft ? R.layout.select_icon_text_item
                    : R.layout.select_text_icon_item);
        }

        @Override
        public int getCount() {
            return mTextItems.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = newView(parent);
            }
            bindView(position, view);
            return view;
        }

        private View newView(ViewGroup parent) {
            View view = mInflater.inflate(mItemLayout, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.textView = (TextView) view.findViewById(R.id.text);
            holder.iconView = (ImageView) view.findViewById(R.id.icon);
            view.setTag(holder);
            return view;
        }

        private void bindView(int position, View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.textView.setText(mTextItems[position]);
            if (holder.iconView != null) {
                if (mIconItems != null && position < mIconItems.length) {
                    holder.iconView.setVisibility(View.VISIBLE);
                    holder.iconView.setImageResource(mIconItems[position]);
                } else {
                    holder.iconView.setVisibility(View.GONE);
                }
            }
        }

        private class ViewHolder {
            public TextView textView;
            public ImageView iconView;
        }
    }
}

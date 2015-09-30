package com.ekuater.labelchat.ui.fragment.labels;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.SystemLabel;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.WeeklyHotLabelMessage;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.ui.UILauncher;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LinYong
 */
public class WeeklyHotLabelFragment extends Fragment {

    public static final String EXTRA_MESSAGE_ID = "message_id";

    private LabelAdapter mAdapter;
    private final AdapterView.OnItemClickListener mItemClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (parent.getAdapter() == mAdapter) {
             //   UILauncher.launchLabelOptionUI(getFragmentManager(),
                  //      mAdapter.getItem(position).getName());
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.weekly_hot_label);
        }

        mAdapter = new LabelAdapter(getActivity());
        loadMessage();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_label_weekly_hot, container, false);
        ListView listView = (ListView) view.findViewById(R.id.label_list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(mItemClickListener);
        return view;
    }

    private void loadMessage() {
        Bundle args = getArguments();
        long messageId = -1;
        PushMessageManager pushMessageManager = PushMessageManager.getInstance(getActivity());

        if (args != null) {
            messageId = args.getLong(EXTRA_MESSAGE_ID, messageId);
        }

        SystemPush push = pushMessageManager.getPushMessage(messageId);
        if (push != null) {
            WeeklyHotLabelMessage hotLabelMessage = WeeklyHotLabelMessage.build(push);
            if (hotLabelMessage != null) {
                List<SystemLabel> list = new ArrayList<SystemLabel>();
                for (SystemLabel label : hotLabelMessage.getHotLabels()) {
                    if (label != null) {
                        list.add(label);
                    }
                }
                mAdapter.updateLabelList(list);
            }
        }
        pushMessageManager.updatePushMessageProcessed(messageId);
    }

    private static class LabelAdapter extends BaseAdapter {

        private List<SystemLabel> mLabelsList = new ArrayList<SystemLabel>();
        private LayoutInflater mInflater;
        private Context mContext;

        public LabelAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        public void updateLabelList(List<SystemLabel> list) {
            mLabelsList = list;
            notifyDataSetChanged();
        }

        public void addLabelList(List<SystemLabel> list) {
            mLabelsList.addAll(list);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mLabelsList.size();
        }

        @Override
        public SystemLabel getItem(int position) {
            return mLabelsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.hot_label_item, parent, false);
            }
            bindView(convertView, position);
            return convertView;
        }

        private void bindView(View view, int position) {
            SystemLabel label = getItem(position);
            TextView nameText = (TextView) view.findViewById(R.id.name);
            TextView countText = (TextView) view.findViewById(R.id.count);

            nameText.setText(label.getName());
            countText.setText(mContext.getString(R.string.label_use_count,
                    label.getTotalUser()));
        }
    }
}

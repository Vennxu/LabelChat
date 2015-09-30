package com.ekuater.labelchat.ui.fragment.main;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.DiscoverListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wenxiang on 2015/3/3.
 *
 * @author XuWenxiang
 */
public class DiscoverFragment extends Fragment {

    private static final String FUNCTION_PEOPLE_AROUND = "function_people_around";
    private static final String FUNCTION_CHAT_ROOM = "function_chat_room";
    private static final String FUNCTION_CONFIDE = "function_confide";
    private static final String FUNCTION_VOICE = "function_voice";

    private DiscoverAdapter mDiscoverAdapter;
    private final DiscoverListItem.FunctionItemListener mFunctionItemListener
            = new DiscoverListItem.FunctionItemListener() {
        @Override
        public void onClick(String function) {
            launchFunctionItemUI(function);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        mDiscoverAdapter = new DiscoverAdapter(activity);
        mDiscoverAdapter.updateItems(newMethodItemList());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(R.string.main_activity_tab_discover);

        ListView listView = (ListView) view.findViewById(R.id.function_list);
        listView.setAdapter(mDiscoverAdapter);
        listView.setOnItemClickListener(mDiscoverAdapter);
        return view;
    }

    private List<DiscoverListItem.Item> newMethodItemList() {
        List<DiscoverListItem.Item> items = new ArrayList<>();

        items.add(new DiscoverListItem.FunctionItem(
                getString(R.string.people_around),
                R.drawable.interist_people_around,
                FUNCTION_PEOPLE_AROUND,
                mFunctionItemListener));
        items.add(new DiscoverListItem.FunctionItem(
                getString(R.string.chat_room),
                R.drawable.interist_chat_room,
                FUNCTION_CHAT_ROOM,
                mFunctionItemListener));
        items.add(new DiscoverListItem.FunctionItem(
                getString(R.string.confide),
                R.drawable.interist_confide,
                FUNCTION_CONFIDE,
                mFunctionItemListener));
        items.add(new DiscoverListItem.FunctionItem(
                getString(R.string.voice),
                R.drawable.interist_confide,
                FUNCTION_VOICE,
                mFunctionItemListener));
        return items;
    }

    private void launchFunctionItemUI(String function) {
        switch (function) {
            case FUNCTION_PEOPLE_AROUND:
                UILauncher.launchPeopleAroundUI(getActivity());
                break;
            case FUNCTION_CHAT_ROOM:
                UILauncher.launchChatRoomUI(getActivity());
                break;
            case FUNCTION_CONFIDE:
                UILauncher.launchConfideUI(getActivity(), false);
                break;
            case FUNCTION_VOICE:
                UILauncher.launchVoiceUI(getActivity());
                break;
            default:
                break;
        }
    }

    private static class DiscoverAdapter extends BaseAdapter
            implements AdapterView.OnItemClickListener {

        private final LayoutInflater mInflater;
        private List<DiscoverListItem.Item> mItemList;

        public DiscoverAdapter(Context context) {
            super();
            mInflater = LayoutInflater.from(context);
            mItemList = new ArrayList<>();
        }

        public synchronized void updateItems(List<DiscoverListItem.Item> items) {
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
        public DiscoverListItem.Item getItem(int position) {
            return mItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return DiscoverListItem.getViewTypeCount();
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).getViewType();
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            DiscoverListItem.Item item = getItem(position);
            if (view == null) {
                view = item.newView(mInflater, parent);
            }
            item.bindView(view);
            return view;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            getItem(position).onClick();
        }
    }
}

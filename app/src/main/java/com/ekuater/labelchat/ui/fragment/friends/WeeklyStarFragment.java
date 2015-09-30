package com.ekuater.labelchat.ui.fragment.friends;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.WeeklyStarsMessage;
import com.ekuater.labelchat.datastruct.WeeklyStarsMessage.WeeklyStar;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.util.MiscUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author LinYong
 */
public class WeeklyStarFragment extends Fragment {

    public static final String EXTRA_MESSAGE_ID = "message_id";

    private StarAdapter mAdapter;

    private final AbsListView.OnItemClickListener mItemClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Object object = parent.getItemAtPosition(position);
            if (object instanceof Stranger) {
                Stranger stranger = (Stranger) object;
                UILauncher.launchStrangerDetailUI(view.getContext(), stranger);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.weekly_star);
        }

        mAdapter = new StarAdapter(getActivity());
        loadMessage();
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
            WeeklyStarsMessage weeklyStarMessage = WeeklyStarsMessage.build(push);
            if (weeklyStarMessage != null) {
                List<WeeklyStar> list = new ArrayList<WeeklyStar>();
                for (WeeklyStar star : weeklyStarMessage.getStars()) {
                    if (star != null) {
                        list.add(star);
                    }
                }
                mAdapter.updateStarList(list);
            }
        }
        pushMessageManager.updatePushMessageProcessed(messageId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_weekly_star, container, false);
        ListView listView = (ListView) view.findViewById(R.id.star_list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(mItemClickListener);
        return view;
    }


    private static class StarAdapter extends BaseAdapter {

        private static class WeeklyStarComparator implements Comparator<WeeklyStar> {

            @Override
            public int compare(WeeklyStar lhs, WeeklyStar rhs) {
                return rhs.getNewFriendCount() - lhs.getNewFriendCount();
            }
        }

        private final LayoutInflater mInflater;
        private final WeeklyStarComparator mStarComparator;
        private final AvatarManager mAvatarManager;
        private List<WeeklyStar> mStarList = new ArrayList<WeeklyStar>();

        public StarAdapter(Context context) {
            super();
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mStarComparator = new WeeklyStarComparator();
            mAvatarManager = AvatarManager.getInstance(context);
        }

        public synchronized void updateStarList(List<WeeklyStar> list) {
            mStarList = list;
            Collections.sort(mStarList, mStarComparator);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mStarList.size();
        }

        @Override
        public WeeklyStar getItem(int position) {
            return mStarList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.weekly_star_item, parent, false);
            }
            bindView(convertView, position);
            return convertView;
        }

        private void bindView(View view, int position) {
            WeeklyStar star = getItem(position);
            TextView rankingText = (TextView) view.findViewById(R.id.ranking);
            TextView nicknameText = (TextView) view.findViewById(R.id.nickname);
            TextView countText = (TextView) view.findViewById(R.id.new_friend_count);
            ImageView avatarImage = (ImageView) view.findViewById(R.id.avatar_image);

            rankingText.setText(String.valueOf(position + 1));
            nicknameText.setText(star.getShowName());
            countText.setText(String.valueOf(star.getNewFriendCount()));
            MiscUtils.showAvatarThumb(mAvatarManager, star.getAvatarThumb(), avatarImage);
        }
    }
}

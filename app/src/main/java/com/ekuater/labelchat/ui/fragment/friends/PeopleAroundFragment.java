package com.ekuater.labelchat.ui.fragment.friends;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.util.MiscUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LinYong
 */
public class PeopleAroundFragment extends Fragment {

    private static final int MSG_HANDLE_QUERY_RESULT = 101;

    private static class QueryResult {

        public final List<Stranger> strangerList;
        public final boolean remaining;

        public QueryResult(List<Stranger> strangerList, boolean remaining) {
            this.strangerList = strangerList;
            this.remaining = remaining;
        }
    }

    private ContactsManager mContactsManager;
    private StrangerAdapter mStrangerAdapter;
    private ProgressBar mProgressBar;
    private ProgressBar mLoadMoreView;
    private TextView mNoResultText;
    private boolean mLoading;
    private int mNextRequestTime;
    private boolean mRemaining;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_QUERY_RESULT:
                    handleQueryResult(msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    private final AbsListView.OnItemClickListener mItemClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mContactsManager.isInGuestMode()) {
                UILauncher.launchLoginPromptUI(getFragmentManager());
            } else {
                Object object = parent.getItemAtPosition(position);
                if (object instanceof Stranger) {
                    Stranger stranger = (Stranger) object;
                    UILauncher.launchStrangerDetailUI(view.getContext(), stranger);
                }
            }
        }
    };

    private final AbsListView.OnScrollListener mListScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (totalItemCount > 0
                    && view.getLastVisiblePosition() == (totalItemCount - 1)) {
                loadPeopleAround();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();
        actionBar.hide();

        mContactsManager = ContactsManager.getInstance(activity);
        mStrangerAdapter = new StrangerAdapter(activity);
        mNextRequestTime = 1;
        mLoading = false;
        mRemaining = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_people_around, container, false);
        TextView title = (TextView) view.findViewById(R.id.title);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        title.setText(R.string.people_around);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        ListView listView = (ListView) view.findViewById(R.id.friend_list);
        listView.setAdapter(mStrangerAdapter);
        listView.setOnItemClickListener(mItemClickListener);
        listView.setOnScrollListener(mListScrollListener);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
        mLoadMoreView = (ProgressBar) view.findViewById(R.id.load_more_progress);
        mNoResultText = (TextView) view.findViewById(R.id.no_match_result);
        loadPeopleAround();
        return view;
    }

    private void loadPeopleAround() {
        if (mLoading || !mRemaining) {
            return;
        }

        mLoading = true;
        updateLoadProgress();
        ContactsManager.FriendsQueryObserver observer = new ContactsManager.FriendsQueryObserver() {
            @Override
            public void onQueryResult(int result, Stranger[] users, boolean remaining) {
                List<Stranger> strangerList = new ArrayList<Stranger>();
                if (users != null && users.length > 0) {
                    for (Stranger user : users) {
                        if (user != null) {
                            strangerList.add(user);
                        }
                    }
                }

                Message message = mHandler.obtainMessage(MSG_HANDLE_QUERY_RESULT,
                        new QueryResult(strangerList, remaining));
                mHandler.sendMessage(message);
            }
        };
        mContactsManager.queryNearbyFriend(mNextRequestTime, observer);
    }

    private void handleQueryResult(Object object) {
        if (!(object instanceof QueryResult)) {
            return;
        }

        final QueryResult queryResult = (QueryResult) object;
        mStrangerAdapter.addStrangerList(queryResult.strangerList);
        mLoading = false;
        mRemaining = queryResult.remaining;
        mNextRequestTime += mRemaining ? 1 : 0;
        updateLoadProgress();
    }

    private void updateLoadProgress() {
        if (mLoading) {
            if (mStrangerAdapter.getCount() > 0) {
                mLoadMoreView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            } else {
                mLoadMoreView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
            }
            mNoResultText.setVisibility(View.GONE);
        } else {
            mLoadMoreView.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);
            mNoResultText.setVisibility((mStrangerAdapter.getCount() > 0)
                    ? View.GONE : View.VISIBLE);
        }
    }

    private static class StrangerAdapter extends BaseAdapter {

        private final Context mContext;
        private final ContactsManager mContactsManager;
        private final LayoutInflater mInflater;
        private final AvatarManager mAvatarManager;
        private final LocationInfo mMyLocation;
        private final int mFriendColor;
        private final int mStrangerColor;
        private List<Stranger> mStrangerList = new ArrayList<Stranger>();

        public StrangerAdapter(Context context) {
            super();
            mContext = context;
            mContactsManager = ContactsManager.getInstance(mContext);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mAvatarManager = AvatarManager.getInstance(context);
            mMyLocation = AccountManager.getInstance(context).getLocation();

            Resources res = mContext.getResources();
            mFriendColor = res.getColor(R.color.friend_name_color);
            mStrangerColor = res.getColor(R.color.stranger_name_color);
        }

        public synchronized void addStrangerList(List<Stranger> list) {
            mStrangerList.addAll(list);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mStrangerList.size();
        }

        @Override
        public Stranger getItem(int position) {
            return mStrangerList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = newView(parent);
            }
            bindView(convertView, position);
            return convertView;
        }

        private View newView(ViewGroup parent) {
            return mInflater.inflate(R.layout.search_friend_item, parent, false);
        }

        private void bindView(View view, int position) {
            Stranger stranger = getItem(position);
            TextView nameView = (TextView) view.findViewById(R.id.nickname);
            ImageView sexView = (ImageView) view.findViewById(R.id.gender);
            ImageView avatarImage = (ImageView) view.findViewById(R.id.avatar_image);
            TextView distanceView = (TextView) view.findViewById(R.id.distance);
            LocationInfo strangerLocation = stranger.getLocation();

            showName(stranger, nameView);
            sexView.setImageResource(ConstantCode.getSexImageResource(stranger.getSex()));
            MiscUtils.showAvatarThumb(mAvatarManager, stranger.getAvatarThumb(), avatarImage);

            if (mMyLocation != null && strangerLocation != null) {
                distanceView.setText(MiscUtils.getDistanceString(mContext,
                        mMyLocation.getDistance(strangerLocation)));
                distanceView.setVisibility(View.VISIBLE);
            } else {
                distanceView.setVisibility(View.GONE);
            }
        }

        private void showName(Stranger stranger, TextView textView) {
            final UserContact contact = mContactsManager
                    .getUserContactByUserId(stranger.getUserId());
            final String name = (contact != null) ? contact.getShowName()
                    : stranger.getShowName();
            final int color = (contact != null) ? mFriendColor : mStrangerColor;

            textView.setText(name);
            textView.setTextColor(color);
        }
    }
}

package com.ekuater.labelchat.ui.fragment.friends;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.TmpGroup;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.ContactsManager.FriendsQueryObserver;
import com.ekuater.labelchat.delegate.TmpGroupManager;
import com.ekuater.labelchat.delegate.UserLabelManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.ui.fragment.labels.AddLabelDialogFrament;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.ui.widget.CircleImageView;
import com.ekuater.labelchat.ui.widget.GroupFriendListView;
import com.ekuater.labelchat.ui.widget.StrangerFriendGridView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author LinYong
 */
public class SearchFriendByLabelFragment extends Fragment {

    public static final String EXTRA_SEARCH_LABELS = "search_labels";

    private static final int MSG_HANDLE_SEARCH_RESULT = 101;
    private static final int MSG_HANDLE_LOAD_MORE_RESULT = 102;
    private static final int MSG_CREATE_GROUP_RESULT = 103;
    public static final int MSG_ADD_LABEL_RESULT = 104;
    public static final int MSG_ADDING_LABEL_HOLD = 105;


    private StrangerFriendGridView mGridViewList;
    private FriendGridViewAdapter mGridViewAdapter;
    private GroupFriendListView mGroupListView;
    private FriendGroupListViewAdapter mListViewAdapter;
    private ArrayList<Stranger> groupList;
    private boolean isClickable;
    private BaseLabel[] mSearchLabels;

    private SimpleProgressDialog mProgressDialog;
    private ContactsManager mContactsManager;
    private UserLabelManager mUserLabelManager;
    private TmpGroupManager mTmpGroupManager;
    private ProgressBar mLoadMoreProgressBar;
    private ProgressBar mProgressBar;
    private TextView mNoResultText;

    private MenuItem mGroupMenuItem;
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_SEARCH_RESULT:
                    handleSearchResult(msg.obj);
                    break;
                case MSG_HANDLE_LOAD_MORE_RESULT:
                    handleLoadMoreResult(msg.obj);
                    break;
                case MSG_CREATE_GROUP_RESULT:
                    handleCreateGroupRequestResult(msg.arg1, (TmpGroup) msg.obj);
                    break;
                case MSG_ADD_LABEL_RESULT:
                    onAddLabelResult(msg.arg1);
                    break;
                case MSG_ADDING_LABEL_HOLD:
                    break;
                default:
                    break;
            }
        }
    };

    private int mRequestTime;
    private boolean mRemaining;
    private boolean mLoading;

    private final AbsListView.OnScrollListener mListScrollListener = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (mGridViewList == view) {
                if (totalItemCount > 0
                        && mGridViewList.getLastVisiblePosition() == totalItemCount - 1) {
                    loadMoreFriends();
                }
            }
        }
    };
    private final AbsListView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Object object = parent.getItemAtPosition(position);
            if (object instanceof Stranger) {
                Stranger stranger = (Stranger) object;
                UILauncher.launchStrangerDetailUI(getActivity(), stranger);
            }
        }
    };

    private final StrangerFriendGridView.OnDismissCallback mOnDismissCallback = new StrangerFriendGridView.OnDismissCallback() {

        @Override
        public void onDismiss(int dismissPosition) {
            if (mGroupListView.getVisibility() == View.VISIBLE) {
                groupList = new ArrayList<Stranger>();
                if (dismissPosition > -1) {
                    Stranger stranger = mGridViewAdapter.getItem(dismissPosition);
                    groupList.add(stranger);
                    mListViewAdapter.addStrangerList(groupList);

                    groupList = (ArrayList<Stranger>) mListViewAdapter
                            .getStrangerList();
                    if (mListViewAdapter.getCount() > 0) {
                        mGroupMenuItem.setTitle(getString(R.string.launch_group_chat) + "("
                                + mListViewAdapter.getCount()
                                + ")");
                        mGroupMenuItem.setEnabled(true);
                        isClickable = true;
                    }
                    mGridViewAdapter.remove(dismissPosition);
                }
            }
        }
    };
    private GroupFriendListView.OnRemoveListener mOnRemoveListener = new GroupFriendListView.OnRemoveListener() {

        @Override
        public void removeItem(int position) {
            List<Stranger> strangerList = new ArrayList<Stranger>();
            Stranger
                    stranger = mListViewAdapter.getItem(position);
            strangerList.add(stranger);
            mGridViewAdapter.addStrangerList(strangerList);
            int count = mListViewAdapter.getCount();
            Collections.sort(mGridViewAdapter.getStrangerList(),
                    new StrangerFriendLocationInfoComparator());
            mListViewAdapter.remove(position);
            if (count > mListViewAdapter.getCount()) {
                mGroupMenuItem.setTitle(getString(R.string.launch_group_chat) + "("
                        + mListViewAdapter.getCount()
                        + ")");
                if (mListViewAdapter.getCount() < 1) {
                    mGroupMenuItem.setTitle(getString(R.string.launch_group_chat));
                    mGroupMenuItem.setEnabled(false);
                    isClickable = false;
                }
            }
        }
    };
    TmpGroupManager.IListener mTmpGroupmanagerListener = new TmpGroupManager.AbsListener() {
        @Override
        public void onCreateGroupRequestResult(int result, BaseLabel[] labels, TmpGroup group) {
            super.onCreateGroupRequestResult(result, labels, group);
            Message msg = mHandler.obtainMessage(MSG_CREATE_GROUP_RESULT, result, 0, group);
            mHandler.sendMessage(msg);
        }
    };

    UserLabelManager.IListener mUserLabelManagerListener = new UserLabelManager.AbsListener() {
        @Override
        public void onLabelAdded(int result) {
            super.onLabelAdded(result);
            Message msg = mHandler.obtainMessage(MSG_ADD_LABEL_RESULT, result, 0);
            mHandler.sendMessage(msg);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getActivity().getActionBar();
        actionBar.hide();
        parseArguments();

        mContactsManager = ContactsManager.getInstance(getActivity());
        mUserLabelManager = UserLabelManager.getInstance(getActivity());
        mTmpGroupManager = TmpGroupManager.getInstance(getActivity());
        mTmpGroupManager.registerListener(mTmpGroupmanagerListener);

        mGridViewAdapter = new FriendGridViewAdapter(getActivity());
        mListViewAdapter = new FriendGroupListViewAdapter(getActivity());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stranger_friend_show, container, false);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView title = (TextView) view.findViewById(R.id.title);
        initView(view);
        mGridViewList.setAdapter(mGridViewAdapter);
        mGroupListView.setAdapter(mListViewAdapter);
        mGridViewList.setOnItemClickListener(mItemClickListener);
        mGroupListView.setOnItemClickListener(mItemClickListener);
        mGridViewList.setOnScrollListener(mListScrollListener);
        mGridViewList.setOnDismissCallback(mOnDismissCallback);
        mGroupListView.setOnRemoveListener(mOnRemoveListener);
        searchFriendByLabels();
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        if (mSearchLabels != null && mSearchLabels.length > 0) {
            StringBuilder sb = new StringBuilder();
            String wordSeparator = getString(R.string.word_separator);

            for (int i = 0; i <= mSearchLabels.length - 2; ++i) {
                sb.append(mSearchLabels[i].getName());
                sb.append(wordSeparator);
            }
            sb.append(mSearchLabels[mSearchLabels.length - 1].getName());

            title.setText(sb.toString());
        } else {
            title.setText(R.string.exact_search);
        }
        return view;
    }

    private void initView(View view) {
        mGridViewList = (StrangerFriendGridView) view.findViewById(R.id.stranger_friend_show);
        mGroupListView = (GroupFriendListView) view.findViewById(R.id.friend_group_list);
        mLoadMoreProgressBar = (ProgressBar) view.findViewById(R.id.load_more_progress);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
        mNoResultText = (TextView) view.findViewById(R.id.no_match_result);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTmpGroupManager.unregisterListener(mTmpGroupmanagerListener);
    }

    private void parseArguments() {
        Bundle args = getArguments();
        mSearchLabels = null;
        if (args != null) {
            Parcelable[] parcelables = args.getParcelableArray(EXTRA_SEARCH_LABELS);
            if (parcelables != null && parcelables.length > 0) {
                mSearchLabels = new BaseLabel[parcelables.length];
                for (int i = 0; i < parcelables.length; ++i) {
                    mSearchLabels[i] = (BaseLabel) parcelables[i];
                }
            }
        }
    }

    private void handleCreateGroupRequestResult(int result, TmpGroup group) {
        mProgressDialog.dismiss();
        switch (result) {
            case ConstantCode.TMP_GROUP_OPERATION_SUCCESS:
                ShowToast.makeText(getActivity(), R.drawable.emoji_smile, getString(R.string.create_succeed)).show();
                UILauncher.launchChattingUI(getActivity(), group.getGroupId());
                getActivity().finish();
                break;
            case ConstantCode.TMP_GROUP_OPERATION_GROUP_EXIST:
                ShowToast.makeText(getActivity(), R.drawable.emoji_sad, getString(R.string.not_repetition_create)).show();
                UILauncher.launchMainUI(getActivity());
                getActivity().finish();
                break;
            default:
                ShowToast.makeText(getActivity(), R.drawable.emoji_cry, getString(R.string.create_fail)).show();
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.stranger_friend_group_menu, menu);
        mGroupMenuItem = menu.findItem(R.id.menu_stranger_friend_group);
        mGroupMenuItem.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mGroupListView.getVisibility() == View.GONE) {
            mGroupListView.setVisibility(View.VISIBLE);
            if (mGroupMenuItem != null) {
                mGroupMenuItem.setTitle(getString(R.string.launch_group_chat));
                mGroupMenuItem.setEnabled(false);
                mGridViewList.setMove(true);

            }
        } else {
            if (isClickable) {
                if (mListViewAdapter.getCount() <= 1) {
                    ShowToast.makeText(getActivity(), R.drawable.emoji_sad, getString(R.string.group_member_less)).show();
                } else if (mListViewAdapter.getCount() > 1) {
                    UserLabel[] userLabels = mUserLabelManager.getAllLabels();
                    List<String> userLabelList = null;
                    List<String> labelList = null;
                    if (userLabels != null && userLabels.length > 0) {
                        userLabelList = new ArrayList<String>();
                        for (UserLabel userLabelName : userLabels) {
                            userLabelList.add(userLabelName.getName());
                        }
                        labelList = new ArrayList<String>();
                        for (BaseLabel labelName : mSearchLabels) {
                            labelList.add(labelName.getName());
                        }

                        if (userLabelList.containsAll(labelList)) {
                            mProgressDialog = SimpleProgressDialog.newInstance();
                            mProgressDialog.show(getFragmentManager(), SimpleProgressDialog.class.getSimpleName());
                            mTmpGroupManager.createGroupRequest(mSearchLabels, getMembers(groupList));
                        } else {
                            for (int i = 0; i < userLabelList.size(); i++) {
                                labelList.removeAll(userLabelList);
                            }
                            if (labelList != null && labelList.size() > 0) {
                                BaseLabel[] addLabels = new BaseLabel[labelList.size()];
                                for (int i = 0; i < labelList.size(); i++) {
                                    addLabels[i] = new BaseLabel(labelList.get(i), null);
                                }
                                AddLabelDialogFrament.IConfirmListener mConfirmListener = new AddLabelDialogFrament.AbsConfirmListener() {
                                    @Override
                                    public void onConfirm() {
                                        super.onConfirm();
                                        addLabel();
                                    }
                                };
                                AddLabelDialogFrament fragment = AddLabelDialogFrament.newInstance(addLabels, mConfirmListener);
                                fragment.show(getFragmentManager(), "addLabelConfirm");
                            }
                        }
                    } else {
                        AddLabelDialogFrament.IConfirmListener mConfirmListener = new AddLabelDialogFrament.AbsConfirmListener() {
                            @Override
                            public void onConfirm() {
                                super.onConfirm();
                                addLabel();
                            }
                        };
                        AddLabelDialogFrament fragment = AddLabelDialogFrament.newInstance(mSearchLabels, mConfirmListener);
                        fragment.show(getFragmentManager(), "addLabelConfirm");
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private String[] getMembers(ArrayList<Stranger> groupFriendList) {
        String[] members = new String[groupFriendList.size()];
        for (int i = 0; i < groupFriendList.size(); i++) {
            members[i] = groupFriendList.get(i).getUserId();
        }
        return members;
    }

    private static class QueryResult {

        public final ArrayList<Stranger> strangerList;
        public final boolean remaining;

        public QueryResult(ArrayList<Stranger> strangerList, boolean remaining) {
            this.strangerList = strangerList;
            this.remaining = remaining;
        }
    }

    private void searchFriendByLabels() {
        FriendsQueryObserver observer = new FriendsQueryObserver() {
            @Override
            public void onQueryResult(int result, Stranger[] users, boolean remaining) {
                ArrayList<Stranger> strangerList = new ArrayList<Stranger>();

                if (users != null && users.length > 0) {
                    for (Stranger user : users) {
                        if (user != null) {
                            strangerList.add(user);
                        }
                    }
                }

                Message message = mHandler.obtainMessage(MSG_HANDLE_SEARCH_RESULT,
                        new QueryResult(strangerList, remaining));
                mHandler.sendMessage(message);
            }
        };
        mRequestTime = 1;
        mRemaining = false;
        mLoading = true;
        mContactsManager.queryFriendByLabels(mSearchLabels, mRequestTime, observer);
    }

    private void handleSearchResult(Object object) {
        if (!(object instanceof QueryResult)) {
            return;
        }

        final QueryResult queryResult = (QueryResult) object;
        mGridViewAdapter.updataStrangerList(queryResult.strangerList);
        mRemaining = queryResult.remaining;
        mLoading = false;
        mRequestTime += mRemaining ? 1 : 0;
        mNoResultText.setVisibility(mGridViewAdapter.getCount() > 0
                ? View.GONE : View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    private void loadMoreFriends() {
        if (mLoading || !mRemaining) {
            return;
        }

        FriendsQueryObserver observer = new FriendsQueryObserver() {
            @Override
            public void onQueryResult(int result, Stranger[] users, boolean remaining) {
                ArrayList<Stranger> strangerList = new ArrayList<Stranger>();

                if (users != null && users.length > 0) {
                    for (Stranger user : users) {
                        if (user != null) {
                            strangerList.add(user);
                        }
                    }
                }

                Message message = mHandler.obtainMessage(MSG_HANDLE_LOAD_MORE_RESULT,
                        new QueryResult(strangerList, remaining));
                mHandler.sendMessage(message);
            }
        };
        mRemaining = false;
        mContactsManager.queryFriendByLabels(mSearchLabels, mRequestTime, observer);
        mLoadMoreProgressBar.setVisibility(View.VISIBLE);
    }

    private void handleLoadMoreResult(Object object) {
        if (!(object instanceof QueryResult)) {
            return;
        }

        final QueryResult queryResult = (QueryResult) object;
        mGridViewAdapter.addStrangerList(queryResult.strangerList);
        mRemaining = queryResult.remaining;
        mLoading = false;
        mRequestTime += mRemaining ? 1 : 0;
        mLoadMoreProgressBar.setVisibility(View.GONE);
    }

    private void addLabel() {
        mUserLabelManager.registerListener(mUserLabelManagerListener);
        mUserLabelManager.addUserLabels(mSearchLabels);
        mHandler.sendEmptyMessageDelayed(MSG_ADDING_LABEL_HOLD, 10 * 1000);
    }

    private void onAddLabelResult(int result) {
        boolean success = (result == ConstantCode.LABEL_OPERATION_SUCCESS);

        mHandler.removeMessages(MSG_ADDING_LABEL_HOLD);
        if (success == true) {
            ShowToast.makeText(getActivity(), R.drawable.emoji_smile, getString(R.string.add_label_success)).show();
        } else {
            ShowToast.makeText(getActivity(), R.drawable.emoji_cry, getString(R.string.add_label_failure)).show();
        }


        if (success) {
            mProgressDialog = SimpleProgressDialog.newInstance();
            mProgressDialog.show(getFragmentManager(), SimpleProgressDialog.class.getSimpleName());
            mTmpGroupManager.createGroupRequest(mSearchLabels, getMembers(groupList));
        }
        mUserLabelManager.unregisterListener(mUserLabelManagerListener);
    }

    public class StrangerFriendLocationInfoComparator implements Comparator<Stranger> {

        @Override
        public int compare(Stranger stranger1, Stranger stranger2) {
            LocationInfo locationInfo1 = stranger1.getLocation();
            LocationInfo locationInfo2 = stranger2.getLocation();
            if (locationInfo1 == null && locationInfo2 != null) {
                return 1;
            }
            if (locationInfo1 != null && locationInfo2 == null) {
                return -1;

            }
            if (locationInfo1 == null && locationInfo2 == null) {
                return -1;
            } else {
                return (int) (locationInfo1.getDistance(AccountManager.getInstance(
                        getActivity())
                        .getLocation()))
                        - (int) (locationInfo2.getDistance(AccountManager.getInstance(
                        getActivity())
                        .getLocation()));
            }
        }
    }

    public class FriendGridViewAdapter extends BaseAdapter {
        private Context mContext;
        private ContactsManager mContactsManager;
        private LayoutInflater mInflater;
        private AvatarManager mAvatarManger;
        private LocationInfo mLocationInfo;
        private ArrayList<Stranger> mStrangerList = new ArrayList<Stranger>();

        private final int mFriendColor;
        private final int mStrangerColor;

        public ArrayList<Stranger> getStrangerList() {
            return mStrangerList;
        }

        public FriendGridViewAdapter(Context context) {
            this.mContext = context;
            mStrangerList = new ArrayList<Stranger>();
            mContactsManager = ContactsManager.getInstance(context);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mAvatarManger = AvatarManager.getInstance(context);
            mLocationInfo = AccountManager.getInstance(context).getLocation();

            Resources res = mContext.getResources();
            mFriendColor = res.getColor(R.color.friend_name_color);
            mStrangerColor = res.getColor(R.color.stranger_name_color);
        }

        @Override
        public int getCount() {
            return (mStrangerList == null) ? 0 : mStrangerList.size();
        }

        @Override
        public Stranger getItem(int position) {
            return (mStrangerList == null) ? null : mStrangerList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public synchronized void updataStrangerList(ArrayList<Stranger> list) {
            mStrangerList = list;
            notifyDataSetChanged();
        }

        public synchronized void addStrangerList(List<Stranger> list) {
            mStrangerList.addAll(list);
            notifyDataSetChanged();
        }

        public synchronized void remove(int position) {
            mStrangerList.remove(position);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ShowFriend showFriend;
            CircleImageView circleImageView = null;
            Stranger stranger = getItem(position);
            LocationInfo strangerLocationInfo = stranger.getLocation();
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.fragment_stranger_multi_friend_item, parent,
                        false);
                showFriend = new ShowFriend();
                showFriend.strangerFriendImage = (ImageView) convertView
                        .findViewById(R.id.stranger_friend_image);
                showFriend.strangerFriendDistance = (TextView) convertView
                        .findViewById(R.id.stranger_friend_distance);
                showFriend.strangerFriendNickname = (TextView) convertView
                        .findViewById(R.id.stranger_friend_nickname);
                showFriend.strangerPraiseQuantity = (TextView) convertView.findViewById(R.id.stranger_friend_praise_quantity);
                showFriend.strangerPraiseImage = (ImageView) convertView.findViewById(R.id.ic_praise);
                convertView.setTag(showFriend);
            } else {
                showFriend = (ShowFriend) convertView.getTag();
            }
            if (showFriend.strangerFriendImage instanceof CircleImageView) {
                circleImageView = (CircleImageView) showFriend.strangerFriendImage;
            }
            int color;
            switch (stranger.getSex()) {
                case ConstantCode.USER_SEX_FEMALE:
                    color = mContext.getResources().getColor(R.color.pink);
                    break;
                default:
                    color = mContext.getResources().getColor(R.color.blue);
                    break;
            }
            circleImageView.setBorderColor(color);
            MiscUtils
                    .showAvatarThumb(mAvatarManger, stranger.getAvatarThumb(),
                            showFriend.strangerFriendImage);

            if (mLocationInfo != null && strangerLocationInfo != null) {
                showFriend.strangerFriendDistance
                        .setText(MiscUtils.getDistanceString(mContext,
                                mLocationInfo.getDistance(strangerLocationInfo)));
            } else {
                showFriend.strangerFriendDistance.setBackgroundColor(mContext.getResources()
                        .getColor(
                                R.color.transparent));
                showFriend.strangerFriendDistance.setText("");
            }

            showName(stranger, showFriend.strangerFriendNickname);

            return convertView;
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

        class ShowFriend {
            ImageView strangerFriendImage;
            TextView strangerFriendDistance;
            TextView strangerFriendNickname;
            TextView strangerPraiseQuantity;
            ImageView strangerPraiseImage;
        }
    }

    public class FriendGroupListViewAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;
        private AvatarManager mAvatarManger;
        private ArrayList<Stranger> mStrangerList = new ArrayList<Stranger>();

        public ArrayList<Stranger> getStrangerList() {
            return mStrangerList;
        }

        public FriendGroupListViewAdapter(Context context) {
            this.mContext = context;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mAvatarManger = AvatarManager.getInstance(context);
        }

        @Override
        public int getCount() {
            return (mStrangerList == null) ? 0 : mStrangerList.size();
        }

        @Override
        public Stranger getItem(int position) {
            return (mStrangerList == null) ? null : mStrangerList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public synchronized void addStrangerList(List<Stranger> list) {
            mStrangerList.addAll(list);
            notifyDataSetChanged();
        }

        public synchronized void remove(int position) {
            mStrangerList.remove(position);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ShowGroupFriend showGroupFriend;
            CircleImageView circleImageView = null;
            Stranger stranger = getItem(position);
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.stranger_group, parent,
                        false);
                showGroupFriend = new ShowGroupFriend();
                showGroupFriend.strangerFriendImage = (ImageView) convertView
                        .findViewById(R.id.stringer_friend_group);
                convertView.setTag(showGroupFriend);
            } else {
                showGroupFriend = (ShowGroupFriend) convertView.getTag();
            }
            if (showGroupFriend.strangerFriendImage instanceof CircleImageView) {
                circleImageView = (CircleImageView) showGroupFriend.strangerFriendImage;
            }

            MiscUtils
                    .showAvatarThumb(mAvatarManger, stranger.getAvatarThumb(),
                            showGroupFriend.strangerFriendImage);
            return convertView;
        }

        class ShowGroupFriend {
            ImageView strangerFriendImage;
        }

    }
}

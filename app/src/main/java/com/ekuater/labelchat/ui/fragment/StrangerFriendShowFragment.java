
package com.ekuater.labelchat.ui.fragment;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.PraiseStranger;
import com.ekuater.labelchat.datastruct.TmpGroup;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.TmpGroupManager;
import com.ekuater.labelchat.delegate.UserLabelManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.adapter.StrangerFriendGridViewAdapter;
import com.ekuater.labelchat.ui.fragment.adapter.StrangerFriendGroupListViewAdapter;
import com.ekuater.labelchat.ui.fragment.labels.AddLabelDialogFrament;
import com.ekuater.labelchat.ui.util.GuidePreferences;
import com.ekuater.labelchat.ui.util.PageGuider;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.ui.widget.GroupFriendListView;
import com.ekuater.labelchat.ui.widget.GroupFriendListView.OnRemoveListener;
import com.ekuater.labelchat.ui.widget.StrangerFriendGridView;
import com.ekuater.labelchat.ui.widget.StrangerFriendGridView.OnDismissCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author FanChong
 */
public class StrangerFriendShowFragment extends Fragment {

    private StrangerFriendGridView gridViewList;
    private GroupFriendListView groupListView;
    private StrangerFriendGridViewAdapter mStrangerFriendGridViewAdapter;
    private StrangerFriendGroupListViewAdapter mStrangerFriendGroupListViewAdapter;
    private ContactsManager mContactManager;
    private BaseLabel[] mSearchLabels;
    private ProgressBar mLoadMoreProgressBar;
    private ProgressBar mProgressBar;
    private TextView mNoResultText;
    private MenuItem mGroupChatMenu;
    private ArrayList<PraiseStranger> groupFriendList;
    private boolean isClickable;
    private boolean isShow;
    private SimpleProgressDialog mProgressDialog;
    private TmpGroupManager mTmpGroupManager;
    private UserLabelManager mUserLabelManager;
    private BaseLabel mLabel;
    private FragmentActivity activity;

    public static final String EXTRA_SEARCH_LABELS = "search_label";
    public static final String SHOW_MENU = "show_menu";

    private static final int MSG_SEARCH_FRIEND_RESULT = 101;
    private static final int MSG_LOAD_MORE_FRIEND_RESULT = 102;
    private static final int MSG_CREATE_GROUP_RESULT = 103;
    public static final int MSG_ADD_LABEL_RESULT = 104;
    public static final int MSG_ADDING_LABEL_HOLD = 105;

    public final TmpGroupManager.IListener mCreateGroupListener = new TmpGroupManager.AbsListener() {

        @Override
        public void onCreateGroupRequestResult(int result, BaseLabel[] labels, TmpGroup group) {
            Message msg = mHandler.obtainMessage(MSG_CREATE_GROUP_RESULT, result, 0, group);
            mHandler.sendMessage(msg);
        }
    };
    public final UserLabelManager.IListener mUserLabelManagerListener = new UserLabelManager.AbsListener() {
        @Override
        public void onLabelAdded(int result) {
            super.onLabelAdded(result);
            Message msg = mHandler.obtainMessage(MSG_ADD_LABEL_RESULT, result, 0);
            mHandler.sendMessage(msg);
        }
    };
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SEARCH_FRIEND_RESULT:
                    handlerSearchFriendResult(msg.obj);
                    break;
                case MSG_LOAD_MORE_FRIEND_RESULT:
                    handlerLoadMoreFriendResult(msg.obj);
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

    private int mRequestCount;
    private boolean mRemaining;
    private boolean mLoad;

    private static class QueryResult {
        public final ArrayList<PraiseStranger> strangers;
        public final boolean remaining;

        public QueryResult(ArrayList<PraiseStranger> strangers, boolean remaining) {
            this.strangers = strangers;
            this.remaining = remaining;
        }
    }

    private void searchFriendResult() {
        ContactsManager.PraiseStrangerObserver observer = new ContactsManager.PraiseStrangerObserver() {
            @Override
            public void onQueryResult(int result, PraiseStranger[] users, boolean remaining) {
                ArrayList<PraiseStranger> strangerList = new ArrayList<PraiseStranger>();
                if (users != null && users.length > 0) {
                    for (PraiseStranger user : users) {
                        if (user != null) {
                            strangerList.add(user);
                        }
                    }
                }
                Message message = mHandler.obtainMessage(MSG_SEARCH_FRIEND_RESULT, new QueryResult(
                        strangerList, remaining));
                mHandler.sendMessage(message);
            }
        };
        mRequestCount = 1;
        mRemaining = false;
        mLoad = true;
        mContactManager.queryLabelPraiseStranger(mLabel.getId(), mRequestCount, observer);

    }

    private void handlerSearchFriendResult(Object object) {
        if (!(object instanceof QueryResult)) {
            return;
        }
        final QueryResult queryResult = (QueryResult) object;
        ArrayList<PraiseStranger> praiseStrangerList = queryResult.strangers;
        Collections.sort(praiseStrangerList, new StrangerPraiseComparator());
        mStrangerFriendGridViewAdapter.updataStrangerList(praiseStrangerList);
        mRemaining = queryResult.remaining;
        mLoad = false;
        mRequestCount += mRemaining ? 1 : 0;
        mProgressBar.setVisibility(View.GONE);
        mNoResultText.setVisibility(mStrangerFriendGridViewAdapter.getCount() > 0 ? View.GONE
                : View.VISIBLE);

    }

    private void loadMoreFriendResult() {
        if (mLoad || !mRemaining) {
            return;
        }
        ContactsManager.PraiseStrangerObserver observer = new ContactsManager.PraiseStrangerObserver() {
            @Override
            public void onQueryResult(int result, PraiseStranger[] users, boolean remaining) {

                ArrayList<PraiseStranger> strangerList = new ArrayList<PraiseStranger>();
                if (users != null && users.length > 0) {
                    for (PraiseStranger user : users) {
                        if (user != null) {
                            strangerList.add(user);
                        }
                    }
                }
                Message message = mHandler.obtainMessage(MSG_LOAD_MORE_FRIEND_RESULT,
                        new QueryResult(strangerList, remaining));
                mHandler.sendMessage(message);
            }
        };
        mRemaining = false;
        mContactManager.queryLabelPraiseStranger(mLabel.getId(), mRequestCount, observer);
        mLoadMoreProgressBar.setVisibility(View.VISIBLE);
    }

    private void handlerLoadMoreFriendResult(Object object) {
        if (!(object instanceof QueryResult)) {
            return;
        }
        final QueryResult queryResult = (QueryResult) object;
        ArrayList<PraiseStranger> praiseStrangerList = queryResult.strangers;
        Collections.sort(praiseStrangerList, new StrangerPraiseComparator());
        mStrangerFriendGridViewAdapter.addStrangerList(praiseStrangerList);
        mRemaining = queryResult.remaining;
        mLoad = false;
        mRequestCount += mRemaining ? 1 : 0;
        mLoadMoreProgressBar.setVisibility(View.GONE);
    }

    private void handleCreateGroupRequestResult(int result, TmpGroup group) {
        mProgressDialog.dismiss();

        switch (result) {
            case ConstantCode.TMP_GROUP_OPERATION_SUCCESS:
                ShowToast.makeText(activity, R.drawable.emoji_smile, getString(R.string.create_succeed)).show();
                UILauncher.launchChattingUI(activity, group.getGroupId());
                activity.finish();
                break;
            case ConstantCode.TMP_GROUP_OPERATION_GROUP_EXIST:
                ShowToast.makeText(activity, R.drawable.emoji_sad, getString(R.string.not_repetition_create)).show();
                UILauncher.launchMainUI(activity);
                activity.finish();
                break;
            default:
                ShowToast.makeText(activity, R.drawable.emoji_cry, getString(R.string.create_fail)).show();
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        activity = getActivity();
        mTmpGroupManager = TmpGroupManager.getInstance(getActivity());
        mTmpGroupManager.registerListener(mCreateGroupListener);
        mUserLabelManager = UserLabelManager.getInstance(getActivity());
        ActionBar actionBar = getActivity().getActionBar();
        parseArguments();
        if (actionBar != null) {
            for (BaseLabel label : mSearchLabels) {
                if (label != null) {
                    actionBar.setTitle(label.getName());
                }
            }
        }
        if (isShow) {
            setHasOptionsMenu(true);
        } else {
            setHasOptionsMenu(false);
        }
        mContactManager = ContactsManager.getInstance(getActivity());
        mStrangerFriendGridViewAdapter = new StrangerFriendGridViewAdapter(getActivity(), false);
        mStrangerFriendGroupListViewAdapter = new StrangerFriendGroupListViewAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater
                .inflate(R.layout.fragment_stranger_friend_show, container, false);
        initView(view);
        searchFriendResult();
        gridViewList.setLine(true);
        gridViewList.setOverScrollMode(View.OVER_SCROLL_NEVER);
        gridViewList.setAdapter(mStrangerFriendGridViewAdapter);
        groupListView.setAdapter(mStrangerFriendGroupListViewAdapter);
        gridViewList.setOnItemClickListener(mOnItemClickListener);
        groupListView.setOnItemClickListener(mOnItemClickListener);
        gridViewList.setOnScrollListener(mOnScrollListener);
        gridViewList.setOnDismissCallback(mOnDismissCallback);
        groupListView.setOnRemoveListener(mOnRemoveListener);
        gridViewList.setMove(false);

        final View guideParent = activity.findViewById(android.R.id.content);
        final String guidedKey = getClass().getName();
        if (!GuidePreferences.isGuided(activity, guidedKey)
                && guideParent instanceof FrameLayout) {
            PageGuider guider = new PageGuider(activity, (FrameLayout) guideParent,
                    R.array.guide_create_group);
            guider.setListener(new PageGuider.Listener() {
                @Override
                public void onShow() {
                    final ActionBar actionBar = activity.getActionBar();
                    if (actionBar != null) {
                        actionBar.hide();
                    }
                }

                @Override
                public void onHide() {
                    final ActionBar actionBar = activity.getActionBar();
                    if (actionBar != null) {
                        actionBar.show();
                    }
                }
            });
            guider.showGuide();
            GuidePreferences.setGuided(activity, guidedKey);
        }

        return view;
    }

    private void initView(View view) {
        gridViewList = (StrangerFriendGridView) view.findViewById(R.id.stranger_friend_show);
        groupListView = (GroupFriendListView) view.findViewById(R.id.friend_group_list);
        mLoadMoreProgressBar = (ProgressBar) view.findViewById(R.id.load_more_progress);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
        mNoResultText = (TextView) view.findViewById(R.id.no_match_result);
        gridViewList.setVerticalScrollBarEnabled(false);
        groupListView.setDividerHeight(0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTmpGroupManager.unregisterListener(mCreateGroupListener);
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Object object = parent.getItemAtPosition(position);
            if (object instanceof PraiseStranger) {
                PraiseStranger praiseStranger = (PraiseStranger) object;
                UILauncher.launchStrangerDetailUI(view.getContext(), praiseStranger.getStranger());
            }
        }
    };
    private OnScrollListener mOnScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                             int totalItemCount) {
            if (gridViewList == view) {
                if (totalItemCount > 0
                        && gridViewList.getLastVisiblePosition() == totalItemCount - 1) {
                    loadMoreFriendResult();
                }
            }

        }
    };
    private OnDismissCallback mOnDismissCallback = new OnDismissCallback() {

        @Override
        public void onDismiss(int dismissPosition) {
            if (groupListView.getVisibility() == View.VISIBLE) {
                groupFriendList = new ArrayList<PraiseStranger>();
                if (dismissPosition > -1) {
                    PraiseStranger praiseStranger = mStrangerFriendGridViewAdapter.getItem(dismissPosition);
                    groupFriendList.add(praiseStranger);
                    mStrangerFriendGroupListViewAdapter.addStrangerList(groupFriendList);

                    groupFriendList = (ArrayList<PraiseStranger>) mStrangerFriendGroupListViewAdapter
                            .getStrangerList();
                    if (mStrangerFriendGroupListViewAdapter.getCount() > 0) {
                        mGroupChatMenu.setTitle(getString(R.string.launch_group_chat) + "("
                                + mStrangerFriendGroupListViewAdapter.getCount()
                                + ")");
                        mGroupChatMenu.setEnabled(true);
                        isClickable = true;
                    }
                    mStrangerFriendGridViewAdapter.remove(dismissPosition);
                }
            }
        }
    };
    private OnRemoveListener mOnRemoveListener = new OnRemoveListener() {

        @Override
        public void removeItem(int position) {
            List<PraiseStranger> strangerList = new ArrayList<PraiseStranger>();
            PraiseStranger
                    praiseStranger = mStrangerFriendGroupListViewAdapter.getItem(position);

            strangerList.add(praiseStranger);
            mStrangerFriendGridViewAdapter.addStrangerList(strangerList);
            int count = mStrangerFriendGroupListViewAdapter.getCount();
            Collections.sort(mStrangerFriendGridViewAdapter.getStrangerList(), new StrangerPraiseComparator());
            mStrangerFriendGroupListViewAdapter.remove(position);
            if (count > mStrangerFriendGroupListViewAdapter.getCount()) {
                mGroupChatMenu.setTitle(getString(R.string.launch_group_chat) + "("
                        + mStrangerFriendGroupListViewAdapter.getCount()
                        + ")");
                if (mStrangerFriendGroupListViewAdapter.getCount() < 1) {
                    mGroupChatMenu.setTitle(getString(R.string.launch_group_chat));
                    mGroupChatMenu.setEnabled(false);
                    isClickable = false;
                }
            }
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.stranger_friend_group_menu, menu);
        mGroupChatMenu = menu.findItem(R.id.menu_stranger_friend_group);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (groupListView.getVisibility() == View.GONE) {
            groupListView.setVisibility(View.VISIBLE);
            if (mGroupChatMenu != null) {
                mGroupChatMenu.setTitle(getString(R.string.launch_group_chat));
                mGroupChatMenu.setEnabled(false);
                gridViewList.setMove(true);

            }
        } else {
            if (isClickable) {
                if (mStrangerFriendGroupListViewAdapter.getCount() <= 1) {
                    ShowToast.makeText(getActivity(), R.drawable.emoji_sad, getString(R.string.group_member_less)).show();
                } else if (mStrangerFriendGroupListViewAdapter.getCount() > 1) {

                    UserLabel[] userLabels = mUserLabelManager.getAllLabels();
                    if (userLabels != null && userLabels.length > 0) {
                        ArrayList<String> labelNameList = new ArrayList<String>();
                        for (UserLabel userLabel : userLabels) {
                            labelNameList.add(userLabel.getName());
                        }
                        if (labelNameList.contains(mLabel.getName())) {
                            mProgressDialog = SimpleProgressDialog.newInstance();
                            mProgressDialog.show(getFragmentManager(), SimpleProgressDialog.class.getSimpleName());
                            mTmpGroupManager.createGroupRequest(mLabel, getUser(groupFriendList));
                        } else {
                            AddLabelDialogFrament.IConfirmListener mConfirmListener = new AddLabelDialogFrament.AbsConfirmListener() {
                                @Override
                                public void onConfirm() {
                                    super.onConfirm();
                                    addLabel();
                                }
                            };
                            AddLabelDialogFrament fragment = AddLabelDialogFrament.newInstance(new BaseLabel[]{mLabel}, mConfirmListener);
                            fragment.show(getFragmentManager(), "addLabelConfirm");
                        }
                    } else {
                        AddLabelDialogFrament.IConfirmListener mConfirmListener = new AddLabelDialogFrament.AbsConfirmListener() {
                            @Override
                            public void onConfirm() {
                                super.onConfirm();
                                addLabel();
                            }
                        };
                        AddLabelDialogFrament fragment = AddLabelDialogFrament.newInstance(new BaseLabel[]{mLabel}, mConfirmListener);
                        fragment.show(getFragmentManager(), "addLabelConfirm");
                    }
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }


    private String[] getUser(ArrayList<PraiseStranger> strangerList) {
        String[] members = new String[strangerList.size()];
        for (int i = 0; i < strangerList.size(); i++) {
            members[i] = strangerList.get(i).getStranger().getUserId();
        }
        return members;
    }

    private void parseArguments() {
        Bundle arguments = getArguments();
        isShow = false;
        if (arguments != null) {
            mLabel = arguments.getParcelable(EXTRA_SEARCH_LABELS);

            mSearchLabels = new BaseLabel[]{mLabel};
            isShow = arguments.getBoolean(SHOW_MENU);
        } else {
            mSearchLabels = null;
        }
    }

    private void addLabel() {
        mUserLabelManager.registerListener(mUserLabelManagerListener);
        mUserLabelManager.addUserLabels(new BaseLabel[]{mLabel});
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
            mTmpGroupManager.createGroupRequest(mSearchLabels, getUser(groupFriendList));
        }
        mUserLabelManager.unregisterListener(mUserLabelManagerListener);
    }

    public class StrangerPraiseComparator implements Comparator<PraiseStranger> {

        @Override
        public int compare(PraiseStranger praiseStranger1, PraiseStranger praiseStranger2) {
            if (praiseStranger1.getPraiseCount() == 0 && praiseStranger2.getPraiseCount() == 0
                    || praiseStranger1.getPraiseCount() == praiseStranger2.getPraiseCount()) {
                LocationInfo locationInfo1 = praiseStranger1.getStranger().getLocation();
                LocationInfo locationInfo2 = praiseStranger2.getStranger().getLocation();
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
            if (praiseStranger1.getPraiseCount() != 0 && praiseStranger2.getPraiseCount() == 0) {
                return -1;
            }

            if (praiseStranger1.getPraiseCount() == 0 && praiseStranger2.getPraiseCount() != 0) {
                return 1;
            } else {
                return praiseStranger2.getPraiseCount() - praiseStranger1.getPraiseCount();
            }
        }
    }
}

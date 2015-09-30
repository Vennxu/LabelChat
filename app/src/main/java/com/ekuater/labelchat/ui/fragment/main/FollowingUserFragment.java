package com.ekuater.labelchat.ui.fragment.main;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.FollowUser;
import com.ekuater.labelchat.delegate.FollowingManager;
import com.ekuater.labelchat.ui.fragment.friends.StrangerHelper;
import com.ekuater.labelchat.ui.widget.LetterSideBar;

import java.util.List;

/**
 * Created by Leo on 2015/3/10.
 *
 * @author LinYong
 */
public class FollowingUserFragment extends Fragment implements Handler.Callback {

    private static final int MSG_FOLLOW_USER_DATA_CHANGED = 101;

    private FollowingManager mFollowingManager;
    private SortContactsAdapter mContactsAdapter;
    private StrangerHelper mStrangerHelper;
    private Handler mHandler;
    private FollowingLoadTask mLoadTask;

    private ListView mSortListView;
    private View mNoFriendView;

    private FollowingManager.IListener mFollowingListener = new FollowingManager.AbsListener() {
        @Override
        public void onFollowUserDataChanged() {
            mHandler.removeMessages(MSG_FOLLOW_USER_DATA_CHANGED);
            mHandler.sendEmptyMessage(MSG_FOLLOW_USER_DATA_CHANGED);
        }
    };

    private ContactsListItem.FollowingItemListener mItemListener
            = new ContactsListItem.FollowingItemListener() {
        @Override
        public void onClick(FollowUser followUser) {
            mStrangerHelper.showStranger(followUser.getUserId());
        }
    };
    private final FollowLoadListener mLoadListener = new FollowLoadListener() {
        @Override
        public void onLoadDone(List<ContactsListItem.Item> items) {
            mContactsAdapter.updateItems(items);
            updateNoFriendViewVisibility();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        mFollowingManager = FollowingManager.getInstance(activity);
        mContactsAdapter = new SortContactsAdapter(activity);
        mStrangerHelper = new StrangerHelper(this);
        mHandler = new Handler(this);
        startQueryContacts();
        mFollowingManager.registerListener(mFollowingListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFollowingManager.unregisterListener(mFollowingListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.following_user_layout, container, false);
        mSortListView = (ListView) rootView.findViewById(R.id.contacts_list);
        mNoFriendView = rootView.findViewById(R.id.no_friend);
        LetterSideBar sideBar = (LetterSideBar) rootView.findViewById(R.id.letter_side_bar);
        TextView letterPromptView = (TextView) rootView.findViewById(R.id.dialog);

        mSortListView.setAdapter(mContactsAdapter);
        mSortListView.setOnItemClickListener(mContactsAdapter);
        sideBar.setLetterChosenPromptView(letterPromptView);
        sideBar.setOnLetterChosenListener(new LetterSideBar.OnLetterChosenListener() {
            @Override
            public void onLetterChosen(String s) {
                // 该字母首次出现的位置
                int position = mContactsAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mSortListView.setSelection(position);
                }
            }
        });
        updateNoFriendViewVisibility();
        return rootView;
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case MSG_FOLLOW_USER_DATA_CHANGED:
                startQueryContacts();
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }

    private void updateNoFriendViewVisibility() {
        if (mNoFriendView != null) {
            mNoFriendView.setVisibility(mContactsAdapter.getCount() > 0
                    ? View.GONE : View.VISIBLE);
        }
    }

    private void startQueryContacts() {
        if (mLoadTask != null && mLoadTask.getStatus() != AsyncTask.Status.FINISHED) {
            mLoadTask.cancel(true);
        }
        mLoadTask = new FollowingLoadTask(getActivity(), mLoadListener, mItemListener);
        mLoadTask.executeInThreadPool();
    }
}

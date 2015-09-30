package com.ekuater.labelchat.ui.fragment.main;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.PersonalUser;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.widget.LetterSideBar;

import java.util.List;

/**
 * Created by Leo on 2015/3/9.
 *
 * @author LinYong
 */
public class ContactsFragment extends Fragment {

    private ListView mSortListView;
    private View mNoFriendView;

    private SortContactsAdapter mContactsAdapter;
    private PushMessageManager mPushMessageManager;
    private ContactsManager mContactsManager;
    private ContactLoadTask mLoadTask;
    private final ContactLoadTask.Listener mContactLoadListener
            = new ContactLoadTask.Listener() {
        @Override
        public void onLoadDone(List<ContactsListItem.Item> items) {
            mContactsAdapter.updateItems(items);
            updateNoFriendViewVisibility();
        }
    };
    private final PushMessageManager.AbsListener mPushMessageManagerListener
            = new PushMessageManager.AbsListener() {
        @Override
        public void onPushMessageDataChanged() {
            startQueryContacts();
        }
    };
    private final ContactsManager.AbsListener mContactsManagerListener
            = new ContactsManager.AbsListener() {
        @Override
        public void onContactDataChanged() {
            startQueryContacts();
        }
    };

    private final ContactsListItem.ContactItemListener mContactItemListener
            = new ContactsListItem.ContactItemListener() {
        @Override
        public void onClick(String userId) {
            UserContact contact = ContactsManager.getInstance(getActivity())
                    .getUserContactByUserId(userId);
            if (contact != null) {
                UILauncher.launchPersonalDetailUI(getActivity(), new PersonalUser(PersonalUser.CONTACT, contact));
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();

        mContactsManager = ContactsManager.getInstance(activity);
        mContactsManager.registerListener(mContactsManagerListener);
        mPushMessageManager = PushMessageManager.getInstance(activity);
        mPushMessageManager.registerListener(mPushMessageManagerListener);
        mContactsAdapter = new SortContactsAdapter(activity);
        startQueryContacts();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.contacts_list_layout, container, false);
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
    public void onDestroy() {
        super.onDestroy();
        mContactsManager.unregisterListener(mContactsManagerListener);
        mPushMessageManager.registerListener(mPushMessageManagerListener);
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
        mLoadTask = new ContactLoadTask(getActivity(), mContactLoadListener);
        mLoadTask.setContactItemListener(mContactItemListener);
        mLoadTask.executeInThreadPool();
    }
}

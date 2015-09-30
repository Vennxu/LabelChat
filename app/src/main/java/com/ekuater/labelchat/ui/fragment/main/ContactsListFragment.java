package com.ekuater.labelchat.ui.fragment.main;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.delegate.ChatManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.widget.LetterSideBar;
import com.ekuater.labelchat.ui.widget.LetterSideBar.OnLetterChosenListener;

import java.util.List;

public class ContactsListFragment extends Fragment {

    private ListView mSortListView;
    private ListView mMethodListView;
    private View mNoFriendView;

    private SortContactsAdapter mContactsAdapter;
    private MessageListAdapter mMessageAdapter;
    private MessageLoadTask mMessageLoadTask;
    private PushMessageManager mPushMessageManager;
    private ChatManager mChatManager;

    private ContactsManager mContactsManager;
    private ContactLoadTask mContactLoadTask;
    private final PushMessageManager.AbsListener mPushMessageManagerListener
            = new PushMessageManager.AbsListener() {
        @Override
        public void onPushMessageDataChanged() {
            startQueryMessages();
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
            UILauncher.launchFriendDetailUI(getActivity(), userId);
        }
    };
    private ChatManager.AbsListener mChatMangerListener = new ChatManager.AbsListener() {
        @Override
        public void onChatMessageDataChanged() {
            super.onChatMessageDataChanged();
            startQueryMessages();
        }
    };
    private final MessageListItem.PrivateChatItemListener mPrivateChatItemListener = new MessageListItem.PrivateChatItemListener() {
        @Override
        public void onClick(MessageListItem.PrivateChatItem item) {
            UILauncher.launchChattingUI(getActivity(), item.getStringId());
        }
    };
    private final MessageListItem.GroupChatItemListener mGroupChatItemListener = new MessageListItem.GroupChatItemListener() {
        @Override
        public void onClick(MessageListItem.GroupChatItem item) {
            UILauncher.launchChattingUI(getActivity(), item.getStringId());
        }
    };
    private final MessageLoadTask.Listener mMessageLoadListener
            = new MessageLoadTask.Listener() {
        @Override
        public void onLoadDone(List<MessageListItem.Item> items) {
            mMessageAdapter.updateItems(items);
        }
    };
    private final ContactLoadTask.Listener mContactLoadListener
            = new ContactLoadTask.Listener() {
        @Override
        public void onLoadDone(List<ContactsListItem.Item> items) {
            mContactsAdapter.updateItems(items);
            updateNoFriendViewVisibility();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        setHasOptionsMenu(true);
        mChatManager = ChatManager.getInstance(activity);
        mContactsManager = ContactsManager.getInstance(activity);
        mContactsManager.registerListener(mContactsManagerListener);
        mPushMessageManager = PushMessageManager.getInstance(activity);
        mPushMessageManager.registerListener(mPushMessageManagerListener);
        mChatManager.registerListener(mChatMangerListener);
        mContactsAdapter = new SortContactsAdapter(activity);
        mMessageAdapter = new MessageListAdapter(activity);
        startQueryMessages();
        startQueryContacts();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v == mMethodListView) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.message_list_item_context_menu, menu);
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        boolean handled = false;
        ContextMenu.ContextMenuInfo menuInfo = item.getMenuInfo();

        if (menuInfo instanceof AdapterView.AdapterContextMenuInfo) {
            AdapterView.AdapterContextMenuInfo adapterMenuInfo
                    = (AdapterView.AdapterContextMenuInfo) menuInfo;
            handled = true;

            switch (item.getItemId()) {
                case R.id.delete:
                    deleteListItem(mMessageAdapter.getItem(adapterMenuInfo.position));
                    break;
                default:
                    handled = false;
                    break;
            }
        }

        return handled || super.onContextItemSelected(item);
    }

    private void deleteListItem(MessageListItem.Item item) {
        item.delete();
    }

    @Override
    public void onStart() {
        super.onStart();
        startQueryMessages();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContactsManager.unregisterListener(mContactsManagerListener);
        mPushMessageManager.registerListener(mPushMessageManagerListener);
        mChatManager.unregisterListener(mChatMangerListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterForContextMenu(mMethodListView);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts_list, container, false);
        mSortListView = (ListView) view.findViewById(R.id.contacts_list);
        mMethodListView = (ListView) view.findViewById(R.id.method_list);
        mNoFriendView = view.findViewById(R.id.no_friend);

        mSortListView.setAdapter(mContactsAdapter);
        mSortListView.setOnItemClickListener(mContactsAdapter);
        mMethodListView.setAdapter(mMessageAdapter);
        registerForContextMenu(mMethodListView);
        mMethodListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Object object = parent.getItemAtPosition(position);
                if (object instanceof MessageListItem.Item) {
                    final MessageListItem.Item item = (MessageListItem.Item) object;
                    item.onClick();
                }
            }
        });

        LetterSideBar sideBar = (LetterSideBar) view.findViewById(R.id.letter_side_bar);
        TextView letterPromptView = (TextView) view.findViewById(R.id.dialog);
        sideBar.setLetterChosenPromptView(letterPromptView);
        // 设置右侧触摸监听
        sideBar.setOnLetterChosenListener(new OnLetterChosenListener() {
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

        RadioGroup pageRadioGroup = (RadioGroup) view.findViewById(R.id.page_selector);
        pageRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switchPage(checkedId);
            }
        });
        switchPage(pageRadioGroup.getCheckedRadioButtonId());

        return view;
    }

    private void switchPage(int checkedId) {
        switch (checkedId) {
            case R.id.radio_method:
                mMethodListView.setVisibility(View.VISIBLE);
                break;
            case R.id.radio_contact:
                mMethodListView.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    private void updateNoFriendViewVisibility() {
        mNoFriendView.setVisibility(mContactsAdapter.getCount() > 0 ? View.GONE : View.VISIBLE);
    }

    private void startQueryContacts() {
        if (mContactLoadTask != null && mContactLoadTask.getStatus() != AsyncTask.Status.FINISHED) {
            mContactLoadTask.cancel(true);
        }
        mContactLoadTask = new ContactLoadTask(getActivity(), mContactLoadListener);
        mContactLoadTask.setContactItemListener(mContactItemListener);
        mContactLoadTask.executeInThreadPool();
    }

    private void startQueryMessages() {
        if (mMessageLoadTask != null && mMessageLoadTask.getStatus() != AsyncTask.Status.FINISHED) {
            mMessageLoadTask.cancel(true);
        }

        Activity activity = getActivity();
        if (activity != null) {
            mMessageLoadTask = new MessageLoadTask(activity, mMessageLoadListener);
            mMessageLoadTask.setGroupChatItemListener(mGroupChatItemListener);
            mMessageLoadTask.setPrivateChatItemListener(mPrivateChatItemListener);
            mMessageLoadTask.executeInThreadPool();
        }
    }
}

package com.ekuater.labelchat.ui.fragment.main;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.delegate.ChatManager;
import com.ekuater.labelchat.delegate.MiscManager;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.ui.UILauncher;

import java.util.List;

/**
 * Created by Leo on 2015/3/9.
 *
 * @author LinYong
 */
public class MessageFragment extends Fragment implements Handler.Callback {

    private static final int MSG_NETWORK_AVAILABLE_CHANGED = 100;

    private Handler mHandler;
    private MiscManager mMiscManager;
    private View mNetworkHintView;
    private MessageListAdapter mMessageAdapter;
    private ListView mMessageListView;
    private MessageLoadTask mLoadTask;
    private PushMessageManager mPushMessageManager;
    private ChatManager mChatManager;

    private final MiscManager.AbsListener mMiscListener = new MiscManager.AbsListener() {
        @Override
        public void onNetworkAvailableChanged(boolean networkAvailable) {
            mHandler.sendEmptyMessage(MSG_NETWORK_AVAILABLE_CHANGED);
        }
    };
    private final MessageLoadTask.Listener mMessageLoadListener
            = new MessageLoadTask.Listener() {
        @Override
        public void onLoadDone(List<MessageListItem.Item> items) {
            mMessageAdapter.updateItems(items);
        }
    };
    private final MessageListItem.PrivateChatItemListener mPrivateChatItemListener
            = new MessageListItem.PrivateChatItemListener() {
        @Override
        public void onClick(MessageListItem.PrivateChatItem item) {
            UILauncher.launchChattingUI(getActivity(), item.getStringId());
        }
    };
    private final MessageListItem.GroupChatItemListener mGroupChatItemListener
            = new MessageListItem.GroupChatItemListener() {
        @Override
        public void onClick(MessageListItem.GroupChatItem item) {
            UILauncher.launchChattingUI(getActivity(), item.getStringId());
        }
    };
    private final PushMessageManager.AbsListener mPushMessageManagerListener
            = new PushMessageManager.AbsListener() {
        @Override
        public void onPushMessageDataChanged() {
            startQueryMessages();
        }
    };
    private ChatManager.AbsListener mChatMangerListener = new ChatManager.AbsListener() {
        @Override
        public void onChatMessageDataChanged() {
            startQueryMessages();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();

        mHandler = new Handler(this);
        mMiscManager = MiscManager.getInstance(activity);
        mMiscManager.registerListener(mMiscListener);
        mChatManager = ChatManager.getInstance(activity);
        mChatManager.registerListener(mChatMangerListener);
        mPushMessageManager = PushMessageManager.getInstance(activity);
        mPushMessageManager.registerListener(mPushMessageManagerListener);
        mMessageAdapter = new MessageListAdapter(activity);
        startQueryMessages();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.message_list_layout, container, false);
        TextView title = (TextView) rootView.findViewById(R.id.title);
        TextView rightTitle = (TextView) rootView.findViewById(R.id.title_right);
        title.setText(R.string.main_activity_tab_message);
        rightTitle.setVisibility(View.VISIBLE);
        rightTitle.setText(R.string.group_send);
        rightTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UILauncher.launchMoodUI(v.getContext());
            }
        });

        mMessageListView = (ListView) rootView.findViewById(R.id.message_list);
        mMessageListView.setAdapter(mMessageAdapter);
        registerForContextMenu(mMessageListView);
        mMessageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

        mNetworkHintView = rootView.findViewById(R.id.network_available_hint);
        mNetworkHintView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNetworkHintViewClick();
            }
        });
        updateNetworkHintVisibility();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        startQueryMessages();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterForContextMenu(mMessageListView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMiscManager.unregisterListener(mMiscListener);
        mPushMessageManager.registerListener(mPushMessageManagerListener);
        mChatManager.unregisterListener(mChatMangerListener);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v == mMessageListView) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.message_list_item_context_menu, menu);
        }
    }

    @Override
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

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case MSG_NETWORK_AVAILABLE_CHANGED:
                updateNetworkHintVisibility();
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }

    private void updateNetworkHintVisibility() {
        if (mNetworkHintView != null) {
            mNetworkHintView.setVisibility(mMiscManager.isNetworkAvailable()
                    ? View.GONE : View.VISIBLE);
        }
    }

    private void onNetworkHintViewClick() {
        if (android.os.Build.VERSION.SDK_INT > 10) {
            startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
        } else {
            startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
        }
    }

    private void deleteListItem(MessageListItem.Item item) {
        item.delete();
    }

    private void startQueryMessages() {
        if (mLoadTask != null && mLoadTask.getStatus() != AsyncTask.Status.FINISHED) {
            mLoadTask.cancel(true);
        }

        Activity activity = getActivity();
        if (activity != null) {
            mLoadTask = new MessageLoadTask(activity, mMessageLoadListener);
            mLoadTask.setGroupChatItemListener(mGroupChatItemListener);
            mLoadTask.setPrivateChatItemListener(mPrivateChatItemListener);
            mLoadTask.executeInThreadPool();
        }
    }
}

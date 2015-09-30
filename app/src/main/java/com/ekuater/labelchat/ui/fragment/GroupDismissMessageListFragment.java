package com.ekuater.labelchat.ui.fragment;

import android.app.ActionBar;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.LocalTmpGroupDismissedMessage;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.SystemPushType;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author FanChong
 */
public class GroupDismissMessageListFragment extends Fragment {

    private PushMessageManager mPushMessageManager;
    private ListView mGroupDismissList;
    private GroupDismissListAdapter mGroupDismissListAdapter;

    public final class LoadGroupDismissMessageTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            final SystemPush[] mSystemPush = mPushMessageManager.getPushMessagesByType(
                    SystemPushType.TYPE_LOCAL_TMP_GROUP_DISMISSED);
            final List<MessageItem> list = new ArrayList<MessageItem>();

            if (mSystemPush != null) {
                for (SystemPush systemPush : mSystemPush) {
                    try {
                        LocalTmpGroupDismissedMessage message = LocalTmpGroupDismissedMessage.build(
                                new JSONObject(systemPush.getContent()));
                        if (message != null) {
                            list.add(new MessageItem(systemPush.getId(), systemPush.getTime(),
                                    systemPush.getState(), message));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            mGroupDismissListAdapter.updateList(list);
            return null;
        }
    }

    private final PushMessageManager.AbsListener mPushMessageListener
            = new PushMessageManager.AbsListener() {
        @Override
        public void onPushMessageDataChanged() {
            super.onPushMessageDataChanged();
            startQueryMessage();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.group_dismissed_inform);
        }
        mGroupDismissListAdapter = new GroupDismissListAdapter(getActivity(), new Handler());
        mPushMessageManager = PushMessageManager.getInstance(getActivity());
        mPushMessageManager.registerListener(mPushMessageListener);
        startQueryMessage();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_list, container, false);
        mGroupDismissList = (ListView) view.findViewById(R.id.message_list);
        registerForContextMenu(mGroupDismissList);
        mGroupDismissList.setAdapter(mGroupDismissListAdapter);
        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v == mGroupDismissList) {
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
                    mPushMessageManager.deletePushMessage(mGroupDismissListAdapter.getItem(
                            adapterMenuInfo.position).getMessageId());
                    break;
                default:
                    handled = false;
                    break;
            }
        }

        return handled || super.onContextItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPushMessageManager.unregisterListener(mPushMessageListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterForContextMenu(mGroupDismissList);
    }

    private void startQueryMessage() {
        new LoadGroupDismissMessageTask().executeOnExecutor(
                LoadGroupDismissMessageTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    public static class MessageItem {

        private final long mMessageId;
        private final long mTime;
        private final LocalTmpGroupDismissedMessage mMessage;
        private int mState;

        public MessageItem(long msgId, long time, int state,
                           LocalTmpGroupDismissedMessage message) {
            mMessageId = msgId;
            mTime = time;
            mState = state;
            mMessage = message;
        }

        public long getMessageId() {
            return mMessageId;
        }

        public long getTime() {
            return mTime;
        }

        public int getState() {
            return mState;
        }

        public void setState(int state) {
            mState = state;
        }

        public LocalTmpGroupDismissedMessage getMessage() {
            return mMessage;
        }
    }

    public class GroupDismissListAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater inflater;
        private Handler mHandler;
        List<MessageItem> itemList = new ArrayList<MessageItem>();

        public GroupDismissListAdapter(Context context, Handler handler) {
            mContext = context;
            mHandler = handler;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public MessageItem getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public synchronized void updateList(List<MessageItem> list) {
            itemList = list;
            notifyDataSetChangedInUI();
        }

        private void notifyDataSetChangedInUI() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.group_dismiss_message_list_item, parent, false);
            }
            bindView(convertView, position);
            return convertView;
        }

        public void bindView(View view, int position) {
            MessageItem item = getItem(position);
            CircleImageView iconView = (CircleImageView) view.findViewById(R.id.avatar_image);
            TextView title = (TextView) view.findViewById(R.id.title);
            TextView showTime = (TextView) view.findViewById(R.id.timestamp);
            iconView.setImageResource(R.drawable.ic_dismissed_inform);
            showTime.setText(getTimeString(item.getMessage().getDismissTime()));
            title.setText(item.getMessage().getGroupName());

            if (item.getState() == SystemPush.STATE_UNPROCESSED) {
                mPushMessageManager.updatePushMessageProcessed(item.getMessageId());
                item.setState(SystemPush.STATE_PROCESSED);
            }
        }

        private String getTimeString(long time) {
            return DateTimeUtils.getMessageDateString(mContext, time);
        }
    }
}

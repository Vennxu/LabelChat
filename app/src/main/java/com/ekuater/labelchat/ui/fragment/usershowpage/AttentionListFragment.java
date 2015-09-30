package com.ekuater.labelchat.ui.fragment.usershowpage;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.BeenFollowedMessage;
import com.ekuater.labelchat.datastruct.FollowUser;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.SystemPushType;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.ui.fragment.ConfirmDialogFragment;
import com.ekuater.labelchat.ui.fragment.friends.StrangerHelper;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2015/4/1.
 */
public class AttentionListFragment extends Fragment {

    private ListView mListView;
    private PushMessageManager mPushMessageManager;
    private AttentionListAdapter adapter;
    private StrangerHelper mStrangerHelper;
    private PushMessageManager.AbsListener pushMessageManagerListener = new PushMessageManager.AbsListener() {
        @Override
        public void onNewSystemPushReceived(SystemPush systemPush) {
            super.onNewSystemPushReceived(systemPush);
            startQuery();
        }

        @Override
        public void onPushMessageDataChanged() {
            super.onPushMessageDataChanged();
            startQuery();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getActionBar().hide();
        mStrangerHelper = new StrangerHelper(getActivity());
        adapter = new AttentionListAdapter(getActivity());
        mPushMessageManager = PushMessageManager.getInstance(getActivity());
        mPushMessageManager.registerListener(pushMessageManagerListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attention_list, container, false);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView rightTitle = (TextView) view.findViewById(R.id.right_title);
        rightTitle.setTextColor(getResources().getColor(R.color.white));
        rightTitle.setText(R.string.clean);
        rightTitle.setVisibility(View.VISIBLE);
        rightTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.getCount() > 0) {
                    showConfirmDialog();
                }
            }
        });
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        title.setText(R.string.pay_attention);
        mListView = (ListView) view.findViewById(R.id.follower_user_list);
        mListView.setAdapter(adapter);
        registerForContextMenu(mListView);
        mListView.setOnItemClickListener(mOnItemClickListener);
        startQuery();
        return view;
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mStrangerHelper.showStranger(adapter.getItem(position).getMessage().getFollowUser().getUserId());
        }
    };


    private void showConfirmDialog() {
        ConfirmDialogFragment.UiConfig uiConfig = new ConfirmDialogFragment.UiConfig(getActivity().getString(R.string.clean_all_attention_message), null);
        ConfirmDialogFragment confirmDialogFragment = ConfirmDialogFragment.newInstance(uiConfig, confirmListener);
        confirmDialogFragment.show(getFragmentManager(), "LabelStoryCommentMessageFragment");
    }

    private ConfirmDialogFragment.AbsConfirmListener confirmListener = new ConfirmDialogFragment.AbsConfirmListener() {
        @Override
        public void onConfirm() {
            mPushMessageManager.deletePushMessageByType(SystemPushType.TYPE_BEEN_FOLLOWED);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onCancel() {
            super.onCancel();
        }

    };


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.message_list_item_context_menu, menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean handler = false;
        ContextMenu.ContextMenuInfo menuInfo = item.getMenuInfo();
        if (menuInfo instanceof AdapterView.AdapterContextMenuInfo) {
            AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
            handler = true;
            switch (item.getItemId()) {
                case R.id.delete:
                    mPushMessageManager.deletePushMessage(adapter.getItem(adapterContextMenuInfo.position).getMessageId());
                    break;
                default:
                    handler = false;
                    break;
            }
        }

        return handler || super.onContextItemSelected(item);
    }

    private void startQuery() {
        new LoadTask().executeOnExecutor(LoadTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    private class LoadTask extends AsyncTask<Void, Void, List<MessageItem>> {

        @Override
        protected List<MessageItem> doInBackground(Void... params) {
            SystemPush[] systemMessages = mPushMessageManager.getPushMessagesByType(SystemPushType.TYPE_BEEN_FOLLOWED);
            List<MessageItem> notifyMessage = new ArrayList<>();
            if (systemMessages != null) {
                for (SystemPush systemPush : systemMessages) {
                    if (systemPush.getState() == SystemPush.STATE_UNPROCESSED) {
                        mPushMessageManager.updatePushMessageProcessed(systemPush.getId());
                        systemPush.setState(SystemPush.STATE_PROCESSED);
                    }
                    BeenFollowedMessage message = BeenFollowedMessage.build(systemPush);
                    if (message != null) {
                        notifyMessage.add(new MessageItem(systemPush.getId(), message));
                    }
                }
            }
            return notifyMessage;
        }

        @Override
        protected void onPostExecute(List<MessageItem> list) {
            adapter.updateMessageItems(list);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPushMessageManager.unregisterListener(pushMessageManagerListener);
        unregisterForContextMenu(mListView);
    }

    private static class MessageItem {

        private long messageId;
        private BeenFollowedMessage message;

        public MessageItem(long messageId, BeenFollowedMessage beenFollowedMessage) {
            this.messageId = messageId;
            message = beenFollowedMessage;
        }

        public long getMessageId() {
            return messageId;
        }

        public BeenFollowedMessage getMessage() {
            return message;
        }
    }

    private static class AttentionListAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater mInflater;
        private List<MessageItem> messageList;
        private AvatarManager mAvatarManager;

        public AttentionListAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
            mAvatarManager = AvatarManager.getInstance(mContext);
        }

        private final Comparator<MessageItem> mComparator = new Comparator<MessageItem>() {
            @Override
            public int compare(MessageItem lhs, MessageItem rhs) {
                long diff = rhs.getMessage().getTime() - lhs.getMessage().getTime();
                diff = (diff != 0) ? diff : (rhs.getMessage().getTime() - lhs.getMessage().getTime());
                return (diff > 0) ? 1 : (diff < 0) ? -1 : 0;
            }
        };

        private void sortUserLabels(List<MessageItem> labelList) {
            Collections.sort(labelList, mComparator);
        }

        public synchronized void updateMessageItems(List<MessageItem> list) {
            messageList = list;
            sortUserLabels(messageList);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return messageList == null ? 0 : messageList.size();
        }

        @Override
        public MessageItem getItem(int position) {
            return messageList == null ? null : messageList.get(position);
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
            bindView(position, convertView);
            return convertView;
        }

        private View newView(ViewGroup parent) {
            View view = mInflater.inflate(R.layout.invite_user_item, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.avatarImage = (CircleImageView) view.findViewById(R.id.avatar_image);
            holder.nickname = (TextView) view.findViewById(R.id.nickname);
            holder.remindMessage = (TextView) view.findViewById(R.id.remind_message);
            holder.time = (TextView) view.findViewById(R.id.time);
            view.setTag(holder);
            return view;
        }

        private void bindView(int position, View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            MessageItem message = getItem(position);
            BeenFollowedMessage followedMessage = message.getMessage();
            final FollowUser user = followedMessage.getFollowUser();
            if (user.getAvatarThumb() != null) {
                MiscUtils.showAvatarThumb(mAvatarManager, user.getAvatarThumb(), holder.avatarImage);
            }
            holder.nickname.setText(user.getNickname());
            switch (followedMessage.getFollowType()) {
                case 0:
                    holder.remindMessage.setText(mContext.getString(R.string.attention_message));
                    break;
                case 1:
                    holder.remindMessage.setText(mContext.getString(R.string.cancel_attention_message));
                    break;
            }
            holder.time.setText(DateTimeUtils.getMessageDateString(mContext, followedMessage.getTime()));
        }

        private class ViewHolder {
            CircleImageView avatarImage;
            TextView nickname, remindMessage, time;
        }
    }
}

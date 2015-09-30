package com.ekuater.labelchat.ui.fragment.userInfo;

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
import com.ekuater.labelchat.datastruct.BeenInvitedMessage;
import com.ekuater.labelchat.datastruct.LiteStranger;
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
import java.util.List;

/**
 * Created by Administrator on 2015/3/26.
 *
 * @author FanChong
 */
public class InviteListFragment extends Fragment {
    private PushMessageManager mPushMessageManager;

    private Context mContext;
    private ListView mListView;
    private InviteUserAdapter adapter;
    private StrangerHelper mStrangerHelper;

    private PushMessageManager.AbsListener mPushMessageManagerListener = new PushMessageManager.AbsListener() {
        @Override
        public void onNewSystemPushReceived(SystemPush systemPush) {
            super.onNewSystemPushReceived(systemPush);
            if (systemPush.getType() == SystemPushType.TYPE_BEEN_INVITED) {
                startQuery();
            }
        }

        @Override
        public void onPushMessageDataChanged() {
            startQuery();
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getActionBar().hide();
        mContext = getActivity();
        mStrangerHelper = new StrangerHelper(getActivity());
        mPushMessageManager = PushMessageManager.getInstance(mContext);
        mPushMessageManager.registerListener(mPushMessageManagerListener);
        adapter = new InviteUserAdapter(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite_user_list, container, false);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView title = (TextView) view.findViewById(R.id.title);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        title.setText(R.string.invitation);
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
        mListView = (ListView) view.findViewById(R.id.invite_user_list);
        mListView.setAdapter(adapter);
        registerForContextMenu(mListView);
        mListView.setOnItemClickListener(mOnItemClickListener);
        startQuery();
        return view;
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mStrangerHelper.showStranger(adapter.getItem(position).getMessage().getStranger().getUserId());
        }
    };

    private void showConfirmDialog() {
        ConfirmDialogFragment.UiConfig uiConfig = new ConfirmDialogFragment.UiConfig(getActivity().getString(R.string.clean_all_invite_message), null);
        ConfirmDialogFragment confirmDialogFragment = ConfirmDialogFragment.newInstance(uiConfig, confirmListener);
        confirmDialogFragment.show(getFragmentManager(), "LabelStoryCommentMessageFragment");
    }

    private ConfirmDialogFragment.AbsConfirmListener confirmListener = new ConfirmDialogFragment.AbsConfirmListener() {
        @Override
        public void onConfirm() {
            mPushMessageManager.deletePushMessageByType(SystemPushType.TYPE_BEEN_INVITED);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPushMessageManager.unregisterListener(mPushMessageManagerListener);
        unregisterForContextMenu(mListView);
    }

    private void startQuery() {
        new LoadTask().executeOnExecutor(LoadTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    private class LoadTask extends AsyncTask<Void, Void, List<MessageItem>> {
        @Override
        protected List<MessageItem> doInBackground(Void... params) {
            SystemPush[] pushMessages = mPushMessageManager.getPushMessagesByType(SystemPushType.TYPE_BEEN_INVITED);
            List<MessageItem> notifyMessageList = new ArrayList<>();
            if (pushMessages != null) {
                for (SystemPush pushMessage : pushMessages) {
                    if (pushMessage.getState() == SystemPush.STATE_UNPROCESSED) {
                        mPushMessageManager.updatePushMessageProcessed(pushMessage.getId());
                        pushMessage.setState(SystemPush.STATE_PROCESSED);
                    }
                    BeenInvitedMessage message = BeenInvitedMessage.build(pushMessage);
                    if (message != null) {
                        notifyMessageList.add(new MessageItem(pushMessage.getId(), message));
                    }

                }
            }
            return notifyMessageList;
        }

        @Override
        protected void onPostExecute(List<MessageItem> messageItems) {
            adapter.updateInviteUserData(messageItems);
        }
    }

    private static class MessageItem {
        private long messageId;

        public BeenInvitedMessage getMessage() {
            return message;
        }

        public long getMessageId() {
            return messageId;
        }

        private BeenInvitedMessage message;

        public MessageItem(long messageId, BeenInvitedMessage beenInvitedMessage) {
            this.messageId = messageId;
            message = beenInvitedMessage;
        }
    }

    private static class InviteUserAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater mInflater;
        private List<MessageItem> messageList;
        private AvatarManager mAvatarManager;

        public InviteUserAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
            mAvatarManager = AvatarManager.getInstance(context);
        }

        public synchronized void updateInviteUserData(List<MessageItem> list) {
            messageList = list;
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
            ViewHolder holder = new ViewHolder();
            View view = mInflater.inflate(R.layout.invite_user_item, parent, false);
            holder.avatarImage = (CircleImageView) view.findViewById(R.id.avatar_image);
            holder.nickname = (TextView) view.findViewById(R.id.nickname);
            holder.remindMessage = (TextView) view.findViewById(R.id.remind_message);
            holder.time = (TextView) view.findViewById(R.id.time);
            view.setTag(holder);
            return view;
        }

        private void bindView(int position, final View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            MessageItem messageItem = getItem(position);
            BeenInvitedMessage beenInvitedMessage = messageItem.getMessage();
            final LiteStranger liteStranger = beenInvitedMessage.getStranger();
            if (liteStranger.getAvatarThumb() != null) {
                MiscUtils.showAvatarThumb(mAvatarManager, liteStranger.getAvatarThumb(), holder.avatarImage);
            }
            holder.nickname.setText(liteStranger.getNickname());
            holder.time.setText(DateTimeUtils.getMessageDateString(mContext, beenInvitedMessage.getTime()));

        }

        private static class ViewHolder {
            CircleImageView avatarImage;
            TextView nickname, remindMessage, time;
        }
    }

}

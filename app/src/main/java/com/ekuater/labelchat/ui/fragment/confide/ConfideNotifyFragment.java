package com.ekuater.labelchat.ui.fragment.confide;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.Confide;
import com.ekuater.labelchat.datastruct.ConfideMessage;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.SystemPushType;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.ConfirmDialogFragment;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;
import com.ekuater.labelchat.ui.widget.emoji.EmojiTextView;
import com.ekuater.labelchat.util.ColorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/4/17.
 *
 * @author FanChong
 */
public class ConfideNotifyFragment extends Fragment {
    public static final String EXTRA_PUSH_TYPE = "extra_push_type";

    private int mPushType;
    private PushMessageManager mPushMessageManager;
    private ListView mListView;
    private ConfideMessageAdapter adapter;

    private PushMessageManager.AbsListener mPushMessageManagerListener = new PushMessageManager.AbsListener() {
        @Override
        public void onNewSystemPushReceived(SystemPush systemPush) {
            super.onNewSystemPushReceived(systemPush);
            startQueryMessage();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getActionBar().hide();
        parseArgument();
        adapter = new ConfideMessageAdapter(getActivity());
        mPushMessageManager = PushMessageManager.getInstance(getActivity());
        mPushMessageManager.registerListener(mPushMessageManagerListener);

    }

    private void parseArgument() {
        Bundle argument = getArguments();
        if (argument != null) {
            mPushType = argument.getInt(EXTRA_PUSH_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confide_message_list, container, false);
        view.findViewById(R.id.icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(getTitle());
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
        mListView = (ListView) view.findViewById(R.id.confide_message_list);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(onItemClickListener);
        registerForContextMenu(mListView);
        startQueryMessage();
        return view;
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            NotifyItem notifyItem;
            Confide confide;

            Object object = parent.getItemAtPosition(position);
            if (object instanceof NotifyItem) {
                notifyItem = (NotifyItem) object;
                confide = notifyItem.getMessage().getConfide();
                UILauncher.launchConfideDetaileUI(ConfideNotifyFragment.this, confide, 0, 0);

            }
        }
    };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.delete_menu, menu);

    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean handled = false;
        ContextMenu.ContextMenuInfo menuInfo = item.getMenuInfo();

        if (menuInfo instanceof AdapterView.AdapterContextMenuInfo) {
            AdapterView.AdapterContextMenuInfo adapterMenuInfo
                    = (AdapterView.AdapterContextMenuInfo) menuInfo;
            ViewHolder holder = (ViewHolder) adapterMenuInfo.targetView.getTag();
            NotifyItem notifyItem = holder.notifyItem;
            handled = true;

            switch (item.getItemId()) {
                case R.id.delete:
                    mPushMessageManager.deletePushMessage(notifyItem.getMessageId());
                    startQueryMessage();
                    break;
                default:
                    handled = false;
                    break;
            }
        }
        return handled || super.onContextItemSelected(item);
    }

    private String getTitle() {
        return getString(mPushType == SystemPushType.TYPE_LOCAL_CONFIDE_PRAISE_NOTIFY
                ? R.string.confide_praise_title : R.string.confide_comment_title);
    }

    private void showConfirmDialog() {
        ConfirmDialogFragment.UiConfig uiConfig;
        if (mPushType == SystemPushType.TYPE_LOCAL_DYNAMIC_COMMENTS_NOTIFY) {
            uiConfig = new ConfirmDialogFragment.UiConfig(getActivity().getString(R.string.clean_all_comment_message), null);
        } else {
            uiConfig = new ConfirmDialogFragment.UiConfig(getActivity().getString(R.string.clean_all_praise_message), null);
        }
        ConfirmDialogFragment confirmDialogFragment = ConfirmDialogFragment.newInstance(uiConfig, confirmListener);
        confirmDialogFragment.show(getFragmentManager(), "LabelStoryCommentMessageFragment");
    }

    private ConfirmDialogFragment.AbsConfirmListener confirmListener = new ConfirmDialogFragment.AbsConfirmListener() {
        @Override
        public void onConfirm() {
            mPushMessageManager.deletePushMessageByType(mPushType);
            adapter.notifyDataSetChanged();
            startQueryMessage();

        }

        @Override
        public void onCancel() {
            super.onCancel();
        }

    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPushMessageManager.unregisterListener(mPushMessageManagerListener);
        unregisterForContextMenu(mListView);
    }

    private void startQueryMessage() {
        new LoadTask().executeOnExecutor(LoadTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    private class LoadTask extends AsyncTask<Void, Void, List<NotifyItem>> {

        @Override
        protected List<NotifyItem> doInBackground(Void... params) {
            SystemPush[] pushMessages = mPushMessageManager.getPushMessagesByType(mPushType);
            List<NotifyItem> list = new ArrayList<>();
            if (pushMessages != null) {
                for (SystemPush pushMessage : pushMessages) {
                    ConfideMessage message = ConfideMessage.build(pushMessage);
                    if (message != null) {
                        list.add(new NotifyItem(pushMessage.getId(), message));
                    }
                    if (pushMessage.getState() == SystemPush.STATE_UNPROCESSED) {
                        mPushMessageManager.updatePushMessageProcessed(pushMessage.getId());
                    }
                }
            }
            return list;

        }

        @Override
        protected void onPostExecute(List<NotifyItem> notifyItems) {
            super.onPostExecute(notifyItems);
            adapter.updateData(notifyItems);
        }
    }

    private class NotifyItem {
        private final long messageId;
        private final ConfideMessage message;

        public long getMessageId() {
            return messageId;
        }

        public ConfideMessage getMessage() {
            return message;
        }

        public NotifyItem(long messageId, ConfideMessage message) {
            this.messageId = messageId;
            this.message = message;
        }
    }

    private class ConfideMessageAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<NotifyItem> mList;
        private AvatarManager mAvatarManager;
        private Context mContext;

        private ConfideMessageAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            mAvatarManager = AvatarManager.getInstance(context);
            mContext = context;

        }

        public synchronized void updateData(List<NotifyItem> list) {
            mList = list;
            notifyDataSetChanged();
        }

        private String getTimeString(long time) {
            return DateTimeUtils.getMessageDateString(mContext, time);
        }

        @Override
        public int getCount() {
            return mList == null ? 0 : mList.size();
        }

        @Override
        public NotifyItem getItem(int position) {
            return mList == null ? null : mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return convertView;
        }


    }

    private class ViewHolder {
        CircleImageView avatarImage;
        TextView nickname, time, confideContent;
        EmojiTextView commentContent;
        ImageView praiseImage, commentImage, hintView;
        RelativeLayout showContentArea;
        NotifyItem notifyItem;
    }
}

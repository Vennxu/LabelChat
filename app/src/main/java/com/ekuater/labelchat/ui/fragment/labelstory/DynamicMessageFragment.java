package com.ekuater.labelchat.ui.fragment.labelstory;

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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.DynamicOperateMessage;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.SystemPushType;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.ConfirmDialogFragment;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;
import com.ekuater.labelchat.ui.widget.emoji.EmojiTextView;
import com.ekuater.labelchat.ui.widget.emoji.ShowContentTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/4/15.
 *
 * @author FanChong
 */
public class DynamicMessageFragment extends Fragment {
    public static final String EXTRA_PUSH_TYPE = "extra_push_type";

    private int mPushType;
    private ListView mListView;
    private DynamicMessageAdapter adapter;
    private PushMessageManager mPushMessageManager;


    private PushMessageManager.AbsListener pushMessageManagerListener = new PushMessageManager.AbsListener() {

        @Override
        public void onNewSystemPushReceived(SystemPush systemPush) {
            super.onNewSystemPushReceived(systemPush);
            startQueryMessage();
        }
    };

    private void parseArguments() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mPushType = bundle.getInt(EXTRA_PUSH_TYPE);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getActionBar().hide();
        parseArguments();
        adapter = new DynamicMessageAdapter(getActivity());
        mPushMessageManager = PushMessageManager.getInstance(getActivity());
        mPushMessageManager.registerListener(pushMessageManagerListener);
        startQueryMessage();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dynamic_message_list, container, false);
        view.findViewById(R.id.icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(getTitle());
        mListView = (ListView) view.findViewById(R.id.dynamic_message_list);
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
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(onItemClickListener);
        registerForContextMenu(mListView);
        return view;
    }

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


    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            NotifyItem notifyItem;
            Object object = parent.getItemAtPosition(position);
            if (object instanceof NotifyItem) {
                notifyItem = (NotifyItem) object;
                DynamicOperateMessage dynamicMessage = notifyItem.getMessage();
                LabelStory labelStory = new LabelStory();
                labelStory.setLabelStoryId(dynamicMessage.getDynamicId());
                DynamicArguments arguments = new DynamicArguments();
                arguments.setLabelStory(labelStory);
                arguments.setIsShowTitle(true);
                UILauncher.launchFragmentLabelStoryDetaileUI(getActivity(), arguments);
            }
        }
    };


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

    private String getTitle() {
        return getString(mPushType == SystemPushType.TYPE_LOCAL_DYNAMIC_PRAISE_NOTIFY
                ? R.string.dynamic_praise_title : R.string.dynamic_comment_title);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPushMessageManager.unregisterListener(pushMessageManagerListener);
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
                    DynamicOperateMessage notifyMessage = DynamicOperateMessage.build(pushMessage);
                    if (notifyMessage != null) {
                        list.add(new NotifyItem(pushMessage.getState(), pushMessage.getId(), notifyMessage));
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

    private static class NotifyItem {

        private final int state;
        private final long messageId;
        private final DynamicOperateMessage message;


        public NotifyItem(int state, long messageId, DynamicOperateMessage message) {
            this.state = state;
            this.messageId = messageId;
            this.message = message;
        }

        public int getState() {
            return state;
        }

        public long getMessageId() {
            return messageId;
        }

        public DynamicOperateMessage getMessage() {
            return message;
        }

    }

    private static class DynamicMessageAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;
        private List<NotifyItem> mMessageList;
        private AvatarManager mAvatarManager;


        public DynamicMessageAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
            mAvatarManager = AvatarManager.getInstance(mContext);
        }

        public synchronized void updateData(List<NotifyItem> list) {
            mMessageList = list;
            notifyDataSetChanged();
        }

        private String getTimeString(long time) {
            return DateTimeUtils.getMessageDateString(mContext, time);
        }

        @Override
        public int getCount() {
            return mMessageList == null ? 0 : mMessageList.size();
        }

        @Override
        public NotifyItem getItem(int position) {
            return mMessageList == null ? null : mMessageList.get(position);
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

    private static class ViewHolder {
        CircleImageView avatarImage;
        TextView nickname, time, confideContent, voiceName;
        EmojiTextView commentContent;
        ImageView praiseImage, commentImage, hintView;
        RelativeLayout showConfideArea;
        LinearLayout showVoiceArea;
        NotifyItem notifyItem;
    }
}

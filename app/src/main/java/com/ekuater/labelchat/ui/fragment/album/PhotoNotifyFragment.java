package com.ekuater.labelchat.ui.fragment.album;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.AlbumPhoto;
import com.ekuater.labelchat.datastruct.LiteStranger;
import com.ekuater.labelchat.datastruct.PhotoNotifyMessage;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.SystemPushType;
import com.ekuater.labelchat.delegate.AlbumManager;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.ConfirmDialogFragment;
import com.ekuater.labelchat.ui.fragment.friends.StrangerHelper;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2015/3/26.
 *
 * @author LinYong
 */
public class PhotoNotifyFragment extends Fragment implements Handler.Callback {

    public static final String EXTRA_PUSH_TYPE = "extra_push_type";

    private static final int MSG_QUERY_MESSAGES = 101;

    private int mPushType;
    private PushMessageManager mPushManager;
    private NotifyAdapter mNotifyAdapter;
    private Handler mHandler;
    private boolean mLoadingMessages;
    private final PushMessageManager.AbsListener mPushMessageManagerListener
            = new PushMessageManager.AbsListener() {
        @Override
        public void onPushMessageDataChanged() {
            mHandler.removeMessages(MSG_QUERY_MESSAGES);
            mHandler.sendEmptyMessage(MSG_QUERY_MESSAGES);
        }
    };

    private ListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        ActionBar actionBar = activity.getActionBar();
        Bundle args = getArguments();

        mPushType = (args != null) ? args.getInt(EXTRA_PUSH_TYPE) : 0;
        mHandler = new Handler(this);
        mLoadingMessages = false;
        mNotifyAdapter = new NotifyAdapter(activity, new StrangerHelper(this));
        mPushManager = PushMessageManager.getInstance(activity);
        mPushManager.registerListener(mPushMessageManagerListener);
        if (actionBar != null) {
            actionBar.hide();
        }
        startQueryMessages();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPushManager.unregisterListener(mPushMessageManagerListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_photo_notify, container, false);
        ImageView icon = (ImageView) rootView.findViewById(R.id.icon);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView title = (TextView) rootView.findViewById(R.id.title);
        title.setText(getTitle());
        mListView = (ListView) rootView.findViewById(R.id.list);
        TextView rightTitle = (TextView) rootView.findViewById(R.id.right_title);
        rightTitle.setTextColor(getResources().getColor(R.color.white));
        rightTitle.setText(R.string.clean);
        rightTitle.setVisibility(View.VISIBLE);
        rightTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNotifyAdapter.getCount() > 0) {
                    showConfirmDialog();
                }
            }
        });
        mListView.setAdapter(mNotifyAdapter);
        registerForContextMenu(mListView);
        return rootView;
    }

    private void showConfirmDialog() {
        ConfirmDialogFragment.UiConfig uiConfig;

        if (mPushType == SystemPushType.TYPE_LOCAL_PHOTO_NOTIFY_REMINDED) {
            uiConfig = new ConfirmDialogFragment.UiConfig(getActivity().getString(R.string.clean_all_request_message), null);
        } else if (mPushType == SystemPushType.TYPE_LOCAL_PHOTO_NOTIFY_SAW) {
            uiConfig = new ConfirmDialogFragment.UiConfig(getActivity().getString(R.string.clean_all_remind_message), null);
        } else {
            uiConfig = new ConfirmDialogFragment.UiConfig(getActivity().getString(R.string.clean_all_praise_message), null);
        }
        ConfirmDialogFragment confirmDialogFragment = ConfirmDialogFragment.newInstance(uiConfig, confirmListener);
        confirmDialogFragment.show(getFragmentManager(), "LabelStoryCommentMessageFragment");
    }

    private ConfirmDialogFragment.AbsConfirmListener confirmListener = new ConfirmDialogFragment.AbsConfirmListener() {
        @Override
        public void onConfirm() {
            mPushManager.deletePushMessageByType(mPushType);
            mNotifyAdapter.notifyDataSetChanged();
        }

        @Override
        public void onCancel() {
            super.onCancel();
        }

    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterForContextMenu(mListView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final MenuInflater inflater = getActivity().getMenuInflater();

        switch (v.getId()) {
            case R.id.list:
                inflater.inflate(R.menu.delete_menu, menu);
                break;
            default:
                break;
        }
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
                    mPushManager.deletePushMessage(notifyItem.getMessageId());
                    break;
                default:
                    handled = false;
                    break;
            }
        }

        return handled || super.onContextItemSelected(item);
    }

    private String getTitle() {
        String title;
        switch (mPushType) {
            case SystemPushType.TYPE_LOCAL_PHOTO_NOTIFY_SAW:
                title = getString(R.string.photo_notify_title);
                break;
            case SystemPushType.TYPE_LOCAL_PHOTO_NOTIFY_REMINDED:
                title = getString(R.string.photo_request_title);
                break;
            case SystemPushType.TYPE_LOCAL_PHOTO_NOTIFY_PRAISE:
                title = getString(R.string.photo_praise);
                break;
            default:
                title = getString(R.string.app_name);
                break;
        }
        return title;
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case MSG_QUERY_MESSAGES:
                startQueryMessages();
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }

    private void startQueryMessages() {
        if (!mLoadingMessages) {
            new LoadTask().executeOnExecutor(LoadTask.THREAD_POOL_EXECUTOR, (Void) null);
        }
    }

    private void finish() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    private class LoadTask extends AsyncTask<Void, Void, List<NotifyItem>> {
        @Override
        protected void onPreExecute() {
            mLoadingMessages = true;
        }

        @Override
        protected List<NotifyItem> doInBackground(Void... params) {
            SystemPush[] pushMessages = mPushManager.getPushMessagesByType(mPushType);
            List<NotifyItem> notifyMessages = new ArrayList<>();

            if (pushMessages != null) {
                for (SystemPush pushMessage : pushMessages) {
                    PhotoNotifyMessage notifyMessage = PhotoNotifyMessage.build(pushMessage);
                    if (notifyMessage != null) {
                        notifyMessages.add(new NotifyItem(pushMessage.getId(), notifyMessage));
                    }
                    if (pushMessage.getState() == SystemPush.STATE_UNPROCESSED) {
                        mPushManager.updatePushMessageProcessed(pushMessage.getId());
                    }
                }
            }
            return notifyMessages;
        }

        @Override
        protected void onPostExecute(List<NotifyItem> notifyItems) {
            mNotifyAdapter.updateNotifyMessages(notifyItems);
            mLoadingMessages = false;
        }
    }

    private static class NotifyItem {

        private final long messageId;
        private final PhotoNotifyMessage message;

        public NotifyItem(long messageId, PhotoNotifyMessage message) {
            this.messageId = messageId;
            this.message = message;
        }

        public long getMessageId() {
            return messageId;
        }

        public PhotoNotifyMessage getMessage() {
            return message;
        }
    }

    private static class ViewHolder {

        public ImageView avatarView;
        public TextView nicknameView;
        public TextView remindView;
        public TextView timeView;
        public ImageView photoView;

        public NotifyItem notifyItem;
    }

    private static class NotifyAdapter extends BaseAdapter {

        private Context mContext;
        private AlbumManager mAlbumManager;
        private AvatarManager mAvatarManager;
        private LayoutInflater mInflater;
        private StrangerHelper mStrangerHelper;
        private List<NotifyItem> mNotifyItems;

        private final View.OnClickListener mAvatarClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder holder = (ViewHolder) v.getTag();
                PhotoNotifyMessage notifyMessage = holder.notifyItem.getMessage();
                LiteStranger user = notifyMessage.getNotifyUser();
                mStrangerHelper.showStranger(user.getUserId());
            }
        };
        private final View.OnClickListener mPhotoClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder holder = (ViewHolder) v.getTag();
                PhotoNotifyMessage notifyMessage = holder.notifyItem.getMessage();
                AlbumPhoto albumPhoto = notifyMessage.getAlbumPhoto();
                UILauncher.launchMyAlbumGalleryUI(mContext, new AlbumPhoto[]{albumPhoto}, 0);
            }
        };

        public NotifyAdapter(Context context, StrangerHelper strangerHelper) {
            mContext = context;
            mAlbumManager = AlbumManager.getInstance(context);
            mAvatarManager = AvatarManager.getInstance(context);
            mInflater = LayoutInflater.from(context);
            mStrangerHelper = strangerHelper;
        }

        public void updateNotifyMessages(List<NotifyItem> notifyItems) {
            mNotifyItems = notifyItems;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mNotifyItems != null ? mNotifyItems.size() : 0;
        }

        @Override
        public NotifyItem getItem(int position) {
            return mNotifyItems != null ? mNotifyItems.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            NotifyItem notifyItem = getItem(position);
            PhotoNotifyMessage notifyMessage = notifyItem.getMessage();
            LiteStranger user = notifyMessage.getNotifyUser();
            ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.photo_notify_item, parent, false);
                holder = new ViewHolder();
                holder.avatarView = (ImageView) convertView.findViewById(R.id.avatar_image);
                holder.nicknameView = (TextView) convertView.findViewById(R.id.nickname);
                holder.remindView = (TextView) convertView.findViewById(R.id.remind_message);
                holder.timeView = (TextView) convertView.findViewById(R.id.time);
                holder.photoView = (ImageView) convertView.findViewById(R.id.photo);
                convertView.setTag(holder);
                holder.avatarView.setTag(holder);
                holder.photoView.setTag(holder);
                holder.avatarView.setOnClickListener(mAvatarClickListener);
                holder.photoView.setOnClickListener(mPhotoClickListener);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            MiscUtils.showAvatarThumb(mAvatarManager, user.getAvatarThumb(), holder.avatarView);
            holder.notifyItem = notifyItem;
            holder.nicknameView.setText(user.getNickname());
            holder.remindView.setText(getRemindMessage(notifyMessage));
            holder.timeView.setText(DateTimeUtils.getMessageDateString(mContext, notifyMessage.getTime()));
            mAlbumManager.displayPhotoThumb(notifyMessage.getAlbumPhoto().getPhotoThumb(),
                    holder.photoView, R.drawable.pic_loading);
            return convertView;
        }

        private String getRemindMessage(PhotoNotifyMessage notifyMessage) {
            String message;

            switch (notifyMessage.getNotifyType()) {
                case PhotoNotifyMessage.TYPE_HAS_SEEN:
                    message = mContext.getString(R.string.saw_the_photo);
                    break;
                case PhotoNotifyMessage.TYPE_UPLOAD_MORE:
                    message = mContext.getString(R.string.remind_upload_more);
                    break;
                case PhotoNotifyMessage.TYPE_HAS_PRAISE:
                    message = mContext.getString(R.string.praise_the_photo);
                    break;
                default:
                    message = "";
                    break;
            }
            return message;
        }
    }
}

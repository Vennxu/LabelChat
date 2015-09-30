package com.ekuater.labelchat.ui.fragment.main;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ChatManager;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.ui.fragment.push.SystemPushUtils;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;
import com.ekuater.labelchat.ui.widget.PieView;

/*package*/ class MessageListItem {

    public static final int VIEW_TYPE_SYSTEM_MESSAGE = 0;
    public static final int VIEW_TYPE_PRIVATE_CHAT_MESSAGE = 1;
    public static final int VIEW_TYPE_GROUP_CHAT_MESSAGE = 2;
    public static final int VIEW_TYPE_SYSTEM_GROUP_MESSAGE = 3;
    public static final int VIEW_TYPE_COUNT = 4;

    public static int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    public interface Item {

        public View newView(LayoutInflater inflater, ViewGroup parent);

        public void bindView(View view);

        public int getShowViewType();

        public void onClick();

        public long getTime();

        public void delete();
    }

    public interface PrivateChatItemListener {
        public void onClick(PrivateChatItem item);
    }

    public interface GroupChatItemListener {
        public void onClick(GroupChatItem item);
    }

    public abstract static class AbsSystemItem implements Item {

        protected final Context mContext;
        protected final long mMsgId;
        protected final long mTime;
        protected final int mPushType;
        protected final int mNewMsgCount;

        public AbsSystemItem(Context context, SystemPush systemPush) {
            mContext = context;
            mMsgId = systemPush.getId();
            mTime = systemPush.getTime();
            mPushType = systemPush.getType();

            if (isExpandAllType()) {
                mNewMsgCount = (systemPush.getState() == SystemPush.STATE_UNPROCESSED) ? 1 : 0;
            } else {
                mNewMsgCount = PushMessageManager.getInstance(context)
                        .getUnprocessedPushMessageCount(mPushType);
            }
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            return inflater.inflate(R.layout.message_list_item, parent, false);
        }

        @Override
        public void bindView(View view) {
            int defaultIconId = R.drawable.contact_single;
            CircleImageView iconView = (CircleImageView) view.findViewById(R.id.avatar_image);
            TextView title = (TextView) view.findViewById(R.id.title);
            TextView subTitle = (TextView) view.findViewById(R.id.subtitle);
            TextView timeView = (TextView) view.findViewById(R.id.timestamp);
            TextView newMsgHintView = (TextView) view.findViewById(R.id.new_msg_hint);

            title.setText(getTitle());
            String subTitleString = getSubTitle();
            subTitle.setText(subTitleString);
            subTitle.setVisibility(TextUtils.isEmpty(subTitleString) ? View.GONE : View.VISIBLE);
            timeView.setText(getTimeString(getTime()));
            if (getIcon() != 0) {
                iconView.setImageResource(getIcon());
            } else {
                iconView.setImageResource(defaultIconId);
            }
            newMsgHintView.setVisibility((getNewMsgCount() > 0) ? View.VISIBLE : View.GONE);
            if (getNewMsgCount() < 100) {
                newMsgHintView.setText("" + getNewMsgCount());
            } else {
                newMsgHintView.setText("99+");
            }
        }

        @Override
        public int getShowViewType() {
            return VIEW_TYPE_SYSTEM_MESSAGE;
        }

        @Override
        public long getTime() {
            return mTime;
        }

        @Override
        public void delete() {
            if (isExpandAllType()) {
                PushMessageManager.getInstance(mContext).deletePushMessage(mMsgId);
            } else {
                PushMessageManager.getInstance(mContext).deletePushMessageByType(mPushType);
            }
        }

        public boolean isExpandAllType() {
            return false;
        }

        protected abstract String getTitle();

        protected abstract String getSubTitle();

        protected abstract int getIcon();

        protected String getString(int resId) {
            return mContext.getString(resId);
        }

        protected String getString(int resId, Object... formatArgs) {
            return mContext.getString(resId, formatArgs);
        }

        private int getNewMsgCount() {
            return mNewMsgCount;
        }

        private String getTimeString(long time) {
            return DateTimeUtils.getMessageDateString(mContext, time);
        }
    }

    public static class PrivateChatItem implements Item {

        private Context mContext;
        private String mStringId;
        private String mAvatarUrl;
        private String mTitle;
        private String mSubTitle;
        private int mType;
        private long mCurrentTime;
        private int mNewMsgCount = 0;
        private ChatManager mChatManager;
        private AvatarManager mAvatarManager;
        private PrivateChatItemListener mPrivateChatItemListener;

        public PrivateChatItem(Context context, AvatarManager avatarManager,
                               PrivateChatItemListener privateChatItemListener) {
            mContext = context;
            mAvatarManager = avatarManager;
            mPrivateChatItemListener = privateChatItemListener;
        }

        private String getTimeString(long time) {
            return DateTimeUtils.getMessageDateString(mContext, time);
        }

        public String getStringId() {
            return mStringId;
        }

        public void setStringId(String stringId) {
            mStringId = stringId;
        }

        public String getAvatarUrl() {
            return mAvatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            mAvatarUrl = avatarUrl;
        }

        public String getTitle() {
            return mTitle;
        }

        public void setTitle(String title) {
            mTitle = title;
        }

        public String getSubTitle() {
            return mSubTitle;
        }

        public void setSubTitle(String subTitle) {
            mSubTitle = subTitle;
        }

        public int getType() {
            return mType;
        }

        public void setType(int type) {
            this.mType = type;
        }

        public int getNewMsgCount() {
            return mNewMsgCount;
        }

        public void setNewMsgCount(int count) {
            mNewMsgCount = count;
        }

        public long getCurrentTime() {
            return mCurrentTime;
        }

        public void setCurrentTime(long currentTime) {
            mCurrentTime = currentTime;
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            return inflater.inflate(R.layout.message_list_item, parent, false);
        }

        @Override
        public void bindView(View view) {
            final int defaultIconId = R.drawable.contact_single;
            CircleImageView iconView = (CircleImageView) view.findViewById(R.id.avatar_image);
            TextView title = (TextView) view.findViewById(R.id.title);
            TextView subTitle = (TextView) view.findViewById(R.id.subtitle);
            TextView timeView = (TextView) view.findViewById(R.id.timestamp);
            TextView newMsgHintView = (TextView) view.findViewById(R.id.new_msg_hint);
            title.setText(getTitle());
            subTitle.setText(getSubTitle());
            timeView.setText(getTimeString(getCurrentTime()));
            if (!TextUtils.isEmpty(getAvatarUrl())) {
                MiscUtils.showAvatarThumb(mAvatarManager, getAvatarUrl(), iconView, defaultIconId);
            } else {
                iconView.setImageResource(defaultIconId);
            }
            newMsgHintView.setVisibility((getNewMsgCount() > 0) ? View.VISIBLE : View.GONE);
            if (getNewMsgCount() < 100) {
                newMsgHintView.setText("" + getNewMsgCount());
            } else {
                newMsgHintView.setText("99+");
            }
        }

        @Override
        public int getShowViewType() {
            return VIEW_TYPE_PRIVATE_CHAT_MESSAGE;
        }

        @Override
        public void onClick() {
            if (mPrivateChatItemListener != null) {
                mPrivateChatItemListener.onClick(this);
            }
        }

        @Override
        public long getTime() {
            return getCurrentTime();
        }

        @Override
        public void delete() {
            mChatManager = ChatManager.getInstance(mContext);
            mChatManager.clearTargetMessage(getStringId());
        }
    }

    public static class GroupChatItem implements Item {

        private String mStringId;
        private String mAvatarUrl;
        private String mTitle;
        private String mSubTitle;
        private int mType;
        private long mCreateTime;
        private long mCurrentTime;
        private long mTotalDurationTime;
        private int mNewMsgCount = 0;
        private Context mContext;
        private ChatManager mChatManager;
        private AvatarManager mAvatarManager;
        private GroupChatItemListener mGroupChatItemListener;

        public GroupChatItem(Context context, AvatarManager avatarManager,
                             GroupChatItemListener groupChatItemListener) {
            mContext = context;
            mAvatarManager = avatarManager;
            mGroupChatItemListener = groupChatItemListener;
        }

        public String getStringId() {
            return mStringId;
        }

        public void setStringId(String stringId) {
            mStringId = stringId;
        }

        public String getAvatarUrl() {
            return mAvatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            mAvatarUrl = avatarUrl;
        }

        public String getTitle() {
            return mTitle;
        }

        public void setTitle(String title) {
            mTitle = title;
        }

        public String getSubTitle() {
            return mSubTitle;
        }

        public void setSubTitle(String subTitle) {
            mSubTitle = subTitle;
        }

        public long getCreateTime() {
            return mCreateTime;
        }

        public void setCreateTime(long createTime) {
            mCreateTime = createTime;
        }

        public long getCurrentTime() {
            return mCurrentTime;
        }

        public void setCurrentTime(long currentTime) {
            mCurrentTime = currentTime;
        }

        public int getType() {
            return mType;
        }

        public void setType(int type) {
            mType = type;
        }

        public long getTotalDurationTime() {
            return mTotalDurationTime;
        }

        public void setTotalDurationTime(long time) {
            mTotalDurationTime = time;
        }

        public int getNewMsgCount() {
            return mNewMsgCount;
        }

        public void setNewMsgCount(int count) {
            mNewMsgCount = count;
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            return inflater.inflate(R.layout.group_message_list_item, parent, false);
        }

        @Override
        public void bindView(View view) {
            final int defaultIconId = R.drawable.contact_single;
            ImageView iconView = (ImageView) view.findViewById(R.id.avatar_image);
            TextView title = (TextView) view.findViewById(R.id.title);
            TextView subTitle = (TextView) view.findViewById(R.id.subtitle);
            PieView pieView = (PieView) view.findViewById(R.id.pie_time);
            TextView newMsgHintView = (TextView) view.findViewById(R.id.new_msg_hint);
            title.setText(getTitle());
            subTitle.setText(getSubTitle());

            final long totalTime = getTotalDurationTime();
            final long leftTime = totalTime - (System.currentTimeMillis() - getCreateTime());
            final int progress = totalTime > 0 ? (int) (leftTime * 100 / totalTime) : 0;
            pieView.setMax(100);
            pieView.setProgress(progress);

            if (!TextUtils.isEmpty(getAvatarUrl())) {
                MiscUtils.showGroupAvatarThumb(mAvatarManager, getAvatarUrl(),
                        iconView, defaultIconId);
            } else {
                iconView.setImageResource(defaultIconId);
            }
            newMsgHintView.setVisibility((getNewMsgCount() > 0) ? View.VISIBLE : View.GONE);
            if (getNewMsgCount() < 100) {
                newMsgHintView.setText("" + getNewMsgCount());
            } else {
                newMsgHintView.setText("99+");
            }
        }

        @Override
        public int getShowViewType() {
            return VIEW_TYPE_GROUP_CHAT_MESSAGE;
        }

        @Override
        public void onClick() {
            if (mGroupChatItemListener != null) {
                mGroupChatItemListener.onClick(this);
            }
        }

        @Override
        public long getTime() {
            return getCurrentTime();
        }

        @Override
        public void delete() {
            mChatManager = ChatManager.getInstance(mContext);
            mChatManager.clearTargetMessage(getStringId());
        }
    }

    public static PushMessageManager.FliterType fliterType = new PushMessageManager.FliterType() {
        @Override
        public boolean accept(int target, SystemPush push) {
            return target == SystemPushUtils.getNowType(push) ? true : false;
        }
    };

    public abstract static class AbsSystemGroupMessage implements Item {

        protected final Context mContext;
        protected final long mMsgId;
        protected final long mTime;
        protected final int mPushType;
        protected final int mNewMsgCount;

        public AbsSystemGroupMessage(Context context, SystemPush systemPush) {
            mContext = context;
            mMsgId = systemPush.getId();
            mTime = systemPush.getTime();
            mPushType = systemPush.getType();

            if (isExpandAllType()) {
                mNewMsgCount = (systemPush.getState() == SystemPush.STATE_UNPROCESSED) ? 1 : 0;
            } else {
                mNewMsgCount = PushMessageManager.getInstance(context)
                        .getUnprocessedPushMessageCount(SystemPushUtils.getFliterType(SystemPushUtils.getNowType(systemPush)),
                                SystemPushUtils.getNowType(systemPush),fliterType);

            }
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            return inflater.inflate(R.layout.message_list_item, parent, false);
        }

        @Override
        public void bindView(View view) {
            int defaultIconId = R.drawable.contact_single;
            CircleImageView iconView = (CircleImageView) view.findViewById(R.id.avatar_image);
            TextView title = (TextView) view.findViewById(R.id.title);
            TextView subTitle = (TextView) view.findViewById(R.id.subtitle);
            TextView timeView = (TextView) view.findViewById(R.id.timestamp);
            TextView newMsgHintView = (TextView) view.findViewById(R.id.new_msg_hint);

            title.setText(getTitle());
            String subTitleString = getSubTitle();
            subTitle.setText(subTitleString);
            subTitle.setVisibility(TextUtils.isEmpty(subTitleString) ? View.GONE : View.VISIBLE);
            timeView.setText(getTimeString(getTime()));
            if (getIcon() != 0) {
                iconView.setImageResource(getIcon());
            } else {
                iconView.setImageResource(defaultIconId);
            }
            newMsgHintView.setVisibility((getNewMsgCount() > 0) ? View.VISIBLE : View.GONE);
            if (getNewMsgCount() < 100) {
                newMsgHintView.setText("" + getNewMsgCount());
            } else {
                newMsgHintView.setText("99+");
            }
        }

        @Override
        public int getShowViewType() {
            return VIEW_TYPE_SYSTEM_GROUP_MESSAGE;
        }

        @Override
        public long getTime() {
            return mTime;
        }

        @Override
        public void delete() {
            if (isExpandAllType()) {
                PushMessageManager.getInstance(mContext).deletePushMessage(mMsgId);
            } else {
                PushMessageManager.getInstance(mContext).deletePushMessageByType(mPushType);
            }
        }

        public boolean isExpandAllType() {
            return false;
        }

        protected abstract String getTitle();

        protected abstract String getSubTitle();

        protected abstract int getIcon();

        protected String getString(int resId) {
            return mContext.getString(resId);
        }

        protected String getString(int resId, Object... formatArgs) {
            return mContext.getString(resId, formatArgs);
        }

        private int getNewMsgCount() {
            return mNewMsgCount;
        }

        private String getTimeString(long time) {
            return DateTimeUtils.getMessageDateString(mContext, time);
        }
    }

}

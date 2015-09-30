package com.ekuater.labelchat.ui.fragment.main;

import android.content.Context;
import android.support.v4.util.SparseArrayCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ekuater.labelchat.BuildConfig;
import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.AddFriendRejectResultMessage;
import com.ekuater.labelchat.datastruct.BeenFollowedMessage;
import com.ekuater.labelchat.datastruct.BeenInvitedMessage;
import com.ekuater.labelchat.datastruct.BubbleUpMessage;
import com.ekuater.labelchat.datastruct.ConfideMessage;
import com.ekuater.labelchat.datastruct.DynamicOperateMessage;
import com.ekuater.labelchat.datastruct.FollowUser;
import com.ekuater.labelchat.datastruct.LabelRecommendMessage;
import com.ekuater.labelchat.datastruct.LetterMessage;
import com.ekuater.labelchat.datastruct.LiteStranger;
import com.ekuater.labelchat.datastruct.LocalTmpGroupDismissedMessage;
import com.ekuater.labelchat.datastruct.PhotoNotifyMessage;
import com.ekuater.labelchat.datastruct.RegisterWelcomeMessage;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.StrangerRecommendLabelMessage;
import com.ekuater.labelchat.datastruct.SystemLabel;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.SystemPushType;
import com.ekuater.labelchat.datastruct.TodayRecommendedMessage;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.datastruct.ValidateAddFriendMessage;
import com.ekuater.labelchat.datastruct.WeeklyHotLabelMessage;
import com.ekuater.labelchat.datastruct.WeeklyStarConfirmMessage;
import com.ekuater.labelchat.datastruct.WeeklyStarsMessage;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.push.CommentPush;
import com.ekuater.labelchat.ui.fragment.push.PraisePush;
import com.ekuater.labelchat.ui.fragment.push.SystemPushUtils;
import com.ekuater.labelchat.ui.util.DateTimeUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;
import com.ekuater.labelchat.util.L;

import java.lang.reflect.Constructor;

/**
 * @author LinYong
 */
/*package*/ class SystemMsgItem {

    private static final String TAG = SystemMsgItem.class.getSimpleName();
    private static final SparseArrayCompat<Class<? extends MessageListItem.AbsSystemItem>> sItemMap;

    static {
        sItemMap = new SparseArrayCompat<>();

        sItemMap.put(SystemPushType.TYPE_SYSTEM_RECOMMEND_FRIEND, RecommendFriendItem.class);
        sItemMap.put(SystemPushType.TYPE_VALIDATE_ADD_FRIEND, ValidateAddFriendItem.class);
        sItemMap.put(SystemPushType.TYPE_ADD_FRIEND_REJECT_RESULT, AddFriendRejectResultItem.class);
        sItemMap.put(SystemPushType.TYPE_CONFIRM_WEEKLY_STAR, WeeklyStarConfirmItem.class);
        sItemMap.put(SystemPushType.TYPE_WEEKLY_STAR, WeeklyStarItem.class);
        sItemMap.put(SystemPushType.TYPE_BUBBLE_UP, BubbleUpItem.class);
        sItemMap.put(SystemPushType.TYPE_TODAY_RECOMMENDED, TodayRecommendedItem.class);
        sItemMap.put(SystemPushType.TYPE_WEEKLY_HOT_LABEL, HotLabelItem.class);
        sItemMap.put(SystemPushType.TYPE_LOCAL_TMP_GROUP_DISMISSED, GroupDismissedItem.class);
        sItemMap.put(SystemPushType.TYPE_REGISTER_WELCOME, RegisterWelcomeItem.class);
        sItemMap.put(SystemPushType.TYPE_RECOMMEND_LABEL, LabelRecommendItem.class);
        sItemMap.put(SystemPushType.TYPE_STRANGER_RECOMMEND_LABEL, StrangerRecommendLabelItem.class);
//        sItemMap.put(SystemPushType.TYPE_LOCAL_DYNAMIC_PRAISE_NOTIFY, DynamicNotifyItem.class);
//        sItemMap.put(SystemPushType.TYPE_LOCAL_DYNAMIC_COMMENTS_NOTIFY, DynamicNotifyItem.class);
        sItemMap.put(SystemPushType.TYPE_PRIVATE_LETTER, LetterItem.class);
        sItemMap.put(SystemPushType.TYPE_BEEN_FOLLOWED, BeenFollowedItem.class);
        sItemMap.put(SystemPushType.TYPE_LOCAL_PHOTO_NOTIFY_SAW, PhotoNotifyItem.class);
        sItemMap.put(SystemPushType.TYPE_LOCAL_PHOTO_NOTIFY_REMINDED, PhotoNotifyItem.class);
        sItemMap.put(SystemPushType.TYPE_LOCAL_PHOTO_NOTIFY_PRAISE, PhotoNotifyItem.class);
        sItemMap.put(SystemPushType.TYPE_BEEN_INVITED, BeenInvitedItem.class);
        sItemMap.put(SystemPushType.TYPE_LOCAL_CONFIDE_PRAISE_NOTIFY, ConfideMessageItem.class);
        sItemMap.put(SystemPushType.TYPE_LOCAL_CONFIDE_COMMENT_NOTIFY, ConfideMessageItem.class);
    }

    public static MessageListItem.AbsSystemItem build(Context context, SystemPush systemPush) {
        if (systemPush == null) {
            throw new NullPointerException("Build AbsSystemItem empty system push");
        }

        try {
            Class<?> clazz = sItemMap.get(systemPush.getType());
            Constructor<?> constructor = clazz.getConstructor(Context.class, SystemPush.class);
            return (MessageListItem.AbsSystemItem) constructor.newInstance(context, systemPush);
        } catch (Exception e) {
            L.w(TAG, "Unsupported type, return null");
            return BuildConfig.DEBUG ? new UnsupportedItem(context, systemPush) : null;
        }
    }

    public static class UnsupportedItem extends MessageListItem.AbsSystemItem {

        public UnsupportedItem(Context context, SystemPush systemPush) {
            super(context, systemPush);
        }

        @Override
        protected String getTitle() {
            return getString(R.string.unknown);
        }

        @Override
        protected String getSubTitle() {
            return "PushType=" + mPushType;
        }

        @Override
        protected int getIcon() {
            return R.drawable.ic_app_team;
        }

        @Override
        public void onClick() {
        }
    }

    public static class AddFriendRejectResultItem extends MessageListItem.AbsSystemItem {
        private SystemPush mSystemPush;
        private AddFriendRejectResultMessage message;
        private Stranger mStranger;
        private AvatarManager mAvatarManager;

        public AddFriendRejectResultItem(Context context, SystemPush systemPush) {
            super(context, systemPush);
            mSystemPush = systemPush;
            message = AddFriendRejectResultMessage.build(systemPush);
            mStranger = message.getStranger();
            mAvatarManager = AvatarManager.getInstance(context);
        }

        @Override
        protected String getTitle() {
            return mStranger.getNickname();
        }

        @Override
        protected String getSubTitle() {
            return getString(R.string.already_reject_friend_request);
        }

        @Override
        protected int getIcon() {
            return R.drawable.ic_add_friend;
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            return inflater.inflate(R.layout.reject_add_friend_item, parent, false);
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

            timeView.setText(getTimeString(mSystemPush.getTime()));

            if (TextUtils.isEmpty(message.getMessage())) {
                subTitle.setText(getSubTitle());
            } else {
                subTitle.setText(message.getMessage());
            }
            if (!TextUtils.isEmpty(mStranger.getAvatarThumb())) {
                MiscUtils.showAvatarThumb(mAvatarManager, mStranger.getAvatarThumb(), iconView, defaultIconId);
            } else {
                iconView.setImageResource(defaultIconId);
            }
            if (mSystemPush.getState() == SystemPush.STATE_PROCESSED) {
                newMsgHintView.setVisibility(View.GONE);
            }

        }

        @Override
        public boolean isExpandAllType() {
            return true;
        }

        @Override
        public void onClick() {
            UILauncher.launchRejectAddFriend(mContext, mSystemPush.getId());
        }

        private String getTimeString(long time) {
            return DateTimeUtils.getMessageDateString(mContext, time);
        }
    }

    public static class ValidateAddFriendItem extends MessageListItem.AbsSystemItem {

        private String mSubTitle;

        public ValidateAddFriendItem(Context context, SystemPush systemPush) {
            super(context, systemPush);

            ValidateAddFriendMessage message = ValidateAddFriendMessage.build(systemPush);
            mSubTitle = message.getValidateMessage();
        }

        @Override
        protected String getTitle() {
            return getString(R.string.validate_friend);
        }

        @Override
        protected String getSubTitle() {
            return mSubTitle;
        }

        @Override
        protected int getIcon() {
            return R.drawable.ic_add_friend;
        }

        @Override
        public void onClick() {
            UILauncher.launchValidateAddFriendListUI(mContext);
        }
    }

    public static class WeeklyStarConfirmItem extends MessageListItem.AbsSystemItem {

        private String mSubTitle;

        public WeeklyStarConfirmItem(Context context, SystemPush systemPush) {
            super(context, systemPush);

            WeeklyStarConfirmMessage message = WeeklyStarConfirmMessage.build(systemPush);
            if (message != null) {
                mSubTitle = message.getMessage();
            }
        }

        @Override
        protected String getTitle() {
            return getString(R.string.app_team);
        }

        @Override
        protected String getSubTitle() {
            return mSubTitle;
        }

        @Override
        protected int getIcon() {
            return R.drawable.ic_app_team;
        }

        @Override
        public void onClick() {
            UILauncher.launchWeeklyStarConfirmUI(mContext, mMsgId);
        }
    }

    public static class WeeklyStarItem extends MessageListItem.AbsSystemItem {

        private String mSubTitle;

        public WeeklyStarItem(Context context, SystemPush systemPush) {
            super(context, systemPush);

            WeeklyStarsMessage message = WeeklyStarsMessage.build(systemPush);
            if (message != null) {
                final WeeklyStarsMessage.WeeklyStar[] stars = message.getStars();
                final int length = stars.length;
                String nameString = "";

                for (int i = 0; i < length; ++i) {
                    nameString += stars[i].getShowName();
                    if (i != length - 1) {
                        nameString += getString(R.string.word_separator);
                    }
                }
                mSubTitle = nameString.trim();
            }
        }

        @Override
        protected String getTitle() {
            return getString(R.string.weekly_star);
        }

        @Override
        protected String getSubTitle() {
            return mSubTitle;
        }

        @Override
        protected int getIcon() {
            return R.drawable.ic_weekly_star;
        }

        @Override
        public void onClick() {
            UILauncher.launchWeeklyStarUI(mContext, mMsgId);
        }
    }

    public static class BubbleUpItem extends MessageListItem.AbsSystemItem {

        private String mSubTitle;

        public BubbleUpItem(Context context, SystemPush systemPush) {
            super(context, systemPush);

            BubbleUpMessage message = BubbleUpMessage.build(systemPush);
            if (message != null) {
                final Stranger[] strangers = message.getBubbleUpStrangers();
                final int length = strangers.length;
                String nameString = "";

                for (int i = 0; i < length; ++i) {
                    nameString += strangers[i].getShowName();
                    if (i != length - 1) {
                        nameString += getString(R.string.word_separator);
                    }
                }
                mSubTitle = nameString.trim();
            }
        }

        @Override
        protected String getTitle() {
            return getString(R.string.bubble_up);
        }

        @Override
        protected String getSubTitle() {
            return mSubTitle;
        }

        @Override
        protected int getIcon() {
            return R.drawable.ic_bubble_up;
        }

        @Override
        public void onClick() {
            UILauncher.launchBubblingResultUI(mContext, mMsgId);
        }
    }

    public static class TodayRecommendedItem extends MessageListItem.AbsSystemItem {

        private String mSubTitle;

        public TodayRecommendedItem(Context context, SystemPush systemPush) {
            super(context, systemPush);

            TodayRecommendedMessage message = TodayRecommendedMessage.build(systemPush);
            if (message != null) {
                final Stranger[] strangers = message.getRecommendedStrangers();
                final int length = strangers.length;
                String nameString = "";

                for (int i = 0; i < length; ++i) {
                    nameString += strangers[i].getShowName();
                    if (i != length - 1) {
                        nameString += getString(R.string.word_separator);
                    }
                }
                mSubTitle = nameString.trim();
            }
        }

        @Override
        protected String getTitle() {
            return getString(R.string.today_recommended);
        }

        @Override
        protected String getSubTitle() {
            return mSubTitle;
        }

        @Override
        protected int getIcon() {
            return R.drawable.ic_today_recommended;
        }

        @Override
        public void onClick() {
            UILauncher.launchTodayRecommendedUI(mContext, mMsgId);
        }
    }

    public static class HotLabelItem extends MessageListItem.AbsSystemItem {

        private String mSubTitle;

        public HotLabelItem(Context context, SystemPush systemPush) {
            super(context, systemPush);

            WeeklyHotLabelMessage message = WeeklyHotLabelMessage.build(systemPush);
            if (message != null) {
                final SystemLabel[] labels = message.getHotLabels();
                final int length = labels.length;
                String nameString = "";

                for (int i = 0; i < length; ++i) {
                    nameString += labels[i].getName();
                    if (i != length - 1) {
                        nameString += getString(R.string.word_separator);
                    }
                }
                mSubTitle = nameString.trim();
            }
        }

        @Override
        protected String getTitle() {
            return getString(R.string.weekly_hot_label);
        }

        @Override
        protected String getSubTitle() {
            return mSubTitle;
        }

        @Override
        protected int getIcon() {
            return R.drawable.ic_weekly_hot_label;
        }

        @Override
        public void onClick() {
            UILauncher.launchWeeklyHotLabelUI(mContext, mMsgId);
        }
    }

    public static class GroupDismissedItem extends MessageListItem.AbsSystemItem {

        private String mSubTitle;

        public GroupDismissedItem(Context context, SystemPush systemPush) {
            super(context, systemPush);

            LocalTmpGroupDismissedMessage message = LocalTmpGroupDismissedMessage.build(systemPush);
            if (message != null) {
                mSubTitle = message.getGroupName();
            }
        }

        @Override
        protected String getTitle() {
            return getString(R.string.group_dismissed_inform);
        }

        @Override
        protected String getSubTitle() {
            return mSubTitle;
        }

        @Override
        protected int getIcon() {
            return R.drawable.ic_dismissed_inform;
        }

        @Override
        public void onClick() {
            UILauncher.launchGroupDismissListUI(mContext);
        }
    }

    public static class RegisterWelcomeItem extends MessageListItem.AbsSystemItem {

        private int mState;
        private RegisterWelcomeMessage mMessage;

        public RegisterWelcomeItem(Context context, SystemPush systemPush) {
            super(context, systemPush);
            mState = systemPush.getState();
            mMessage = RegisterWelcomeMessage.build(systemPush);
        }

        @Override
        protected String getTitle() {
            return getString(R.string.app_team);
        }

        @Override
        protected String getSubTitle() {
            return (mMessage != null) ? mMessage.getMessage() : null;
        }

        @Override
        protected int getIcon() {
            return R.drawable.ic_app_team;
        }

        @Override
        public void onClick() {
            if (mMessage != null) {
                UILauncher.launchNewUserWelcomesUI(mContext, getTime());
            }
            if (mState == SystemPush.STATE_UNPROCESSED) {
                PushMessageManager.getInstance(mContext).updatePushMessageProcessed(mMsgId);
                mState = SystemPush.STATE_PROCESSED;
            }
        }
    }

    public static class LabelRecommendItem extends MessageListItem.AbsSystemItem {

        private int mState;
        private LabelRecommendMessage mMessage;
        private String mSubTitle;

        public LabelRecommendItem(Context context, SystemPush systemPush) {
            super(context, systemPush);
            mState = systemPush.getState();
            mMessage = LabelRecommendMessage.build(systemPush);

            ContactsManager contactsManager = ContactsManager.getInstance(mContext);
            UserContact contact = contactsManager.getUserContactByUserId(
                    mMessage.getFriendUserId());
            if (contact != null) {
                contact.getShowName();
                mSubTitle = mContext.getString(R.string.someone_label_for_me,
                        contact.getShowName());
            }
        }

        @Override
        protected String getTitle() {
            return getString(R.string.recommend_label_for_me);
        }

        @Override
        protected String getSubTitle() {
            return mSubTitle;
        }

        @Override
        protected int getIcon() {
            return R.drawable.ic_recommend_label;
        }

        @Override
        public boolean isExpandAllType() {
            return true;
        }

        @Override
        public void onClick() {
            if (mMessage != null) {
                UILauncher.launchRecommendLabelShowUI(mContext, mMsgId);
            }

            if (mState == SystemPush.STATE_UNPROCESSED) {
                PushMessageManager.getInstance(mContext).updatePushMessageProcessed(mMsgId);
                mState = SystemPush.STATE_PROCESSED;
            }
        }
    }

    public static class RecommendFriendItem extends MessageListItem.AbsSystemItem {

        private int mState;

        public RecommendFriendItem(Context context, SystemPush systemPush) {
            super(context, systemPush);
        }

        @Override
        protected String getTitle() {
            return getString(R.string.app_team);
        }

        @Override
        protected String getSubTitle() {
            return getString(R.string.recommend_friend_for_me);
        }

        @Override
        protected int getIcon() {
            return R.drawable.ic_stranger_friend;
        }

        @Override
        public boolean isExpandAllType() {
            return true;
        }

        @Override
        public void onClick() {
            UILauncher.launchRecommendFriendShowUI(mContext, mMsgId);
            if (mState == SystemPush.STATE_UNPROCESSED) {
                PushMessageManager.getInstance(mContext).updatePushMessageProcessed(mMsgId);
                mState = SystemPush.STATE_PROCESSED;
            }
        }
    }

    public static class DynamicNotifyItem extends MessageListItem.AbsSystemItem {

        private SystemPush mSystemPush;
        private DynamicOperateMessage mMessage;
        private int mState;

        public DynamicNotifyItem(Context context, SystemPush systemPush) {
            super(context, systemPush);
            mSystemPush = systemPush;
            mMessage = DynamicOperateMessage.build(mSystemPush);
            mState = systemPush.getState();
        }

        @Override
        protected String getTitle() {
            String title;
            if (mMessage != null) {
                switch (mMessage.getOperateType()) {
                    case DynamicOperateMessage.TYPE_OPERATE_PRAISE:
                        title = getString(R.string.dynamic_praise_title);
                        break;
                    case DynamicOperateMessage.TYPE_OPERATE_COMMENT:
                        title = getString(R.string.labelstory_comment_message_title);
                        break;
                    default:
                        title = null;
                        break;
                }
                return title;
            } else {
                return null;
            }

        }

        @Override
        protected String getSubTitle() {
            String subTitle;
            if (mMessage != null) {
                Stranger stranger = mMessage.getStranger();
                String nickname = (stranger != null ? stranger.getNickname() : "");
                switch (mMessage.getOperateType()) {
                    case DynamicOperateMessage.TYPE_OPERATE_PRAISE:
                        subTitle = getString(R.string.praise_message, nickname);
                        break;
                    case DynamicOperateMessage.TYPE_OPERATE_COMMENT:
                        subTitle = TextUtils.isEmpty(mMessage.getReplyDynamicCommentContent())
                                ? getString(R.string.someone_comment_you, nickname)
                                : getString(R.string.someone_reply_your_comment, nickname);
                        break;
                    default:
                        subTitle = null;
                        break;
                }
                return subTitle;
            } else {
                return null;
            }
        }

        @Override
        protected int getIcon() {
            int resId = R.drawable.ic_app_team;
            if (mMessage != null) {
                switch (mMessage.getOperateType()) {
                    case DynamicOperateMessage.TYPE_OPERATE_PRAISE:
                        resId = R.drawable.ic_praise_remind;
                        break;
                    case DynamicOperateMessage.TYPE_OPERATE_COMMENT:
                        resId = R.drawable.ic_story_message;
                    default:
                        break;
                }
            }
            return resId;
        }

        @Override
        public void onClick() {
            UILauncher.launchDynamicNotifyUI(mContext, mPushType);
            if (mState == SystemPush.STATE_UNPROCESSED) {
                PushMessageManager.getInstance(mContext).updatePushMessageProcessed(mMsgId);
                mState = SystemPush.STATE_PROCESSED;
            }
        }
    }

    public static class StrangerRecommendLabelItem extends MessageListItem.AbsSystemItem {

        private int mState;
        private StrangerRecommendLabelMessage mMessage;
        private String mSubTitle;

        public StrangerRecommendLabelItem(Context context, SystemPush systemPush) {
            super(context, systemPush);
            mState = systemPush.getState();
            mMessage = StrangerRecommendLabelMessage.build(systemPush);

            Stranger stranger = mMessage.getStranger();
            if (stranger != null) {
                stranger.getShowName();
                mSubTitle = mContext.getString(R.string.someone_label_for_me,
                        stranger.getShowName());
            }
        }

        @Override
        protected String getTitle() {
            return getString(R.string.recommend_label_for_me);
        }

        @Override
        protected String getSubTitle() {
            return mSubTitle;
        }

        @Override
        protected int getIcon() {
            return R.drawable.ic_recommend_label;
        }

        @Override
        public boolean isExpandAllType() {
            return true;
        }

        @Override
        public void onClick() {
            if (mMessage != null) {
                UILauncher.launchRecommendLabelShowUI(mContext, mMsgId);
            }

            if (mState == SystemPush.STATE_UNPROCESSED) {
                PushMessageManager.getInstance(mContext).updatePushMessageProcessed(mMsgId);
                mState = SystemPush.STATE_PROCESSED;
            }
        }
    }

    public static class LetterItem extends MessageListItem.AbsSystemItem {

        private SystemPush mSystemPush;
        private LetterMessage lcMessage;

        public LetterItem(Context context, SystemPush systemPush) {
            super(context, systemPush);
            mSystemPush = systemPush;
            lcMessage = LetterMessage.build(mSystemPush);
        }

        @Override
        protected String getTitle() {
            return getString(R.string.letter_message);
        }

        @Override
        protected String getSubTitle() {
            if (lcMessage == null) {
                return null;
            }
            Stranger stranger = lcMessage.getStranger();
            String name = "";
            if (stranger != null) {
                name = MiscUtils.getUserRemarkName(mContext, stranger.getUserId());
            }
            String nickname = name != null && name.length() > 0 ? name : stranger != null ? stranger.getNickname() : "";
            if (lcMessage.getTag() == 1) {
                return getString(R.string.letter_send_message, nickname);
            } else {
                return getString(R.string.letter_receive_message, nickname);
            }
        }

        @Override
        protected int getIcon() {
            return R.drawable.ic_mail_receive;
        }

        @Override
        public void onClick() {
            UILauncher.launchLabelStoryLetterCompleterMsgUI(mContext);
        }
    }

    public static class BeenFollowedItem extends MessageListItem.AbsSystemItem {

        private int mState;
        private BeenFollowedMessage mMessage;

        public BeenFollowedItem(Context context, SystemPush systemPush) {
            super(context, systemPush);
            mMessage = BeenFollowedMessage.build(systemPush);
            mState = systemPush.getState();
        }

        @Override
        protected String getTitle() {
            return getString(R.string.pay_attention);
        }

        @Override
        protected String getSubTitle() {
            FollowUser user = mMessage.getFollowUser();
            String nickname = user != null ? user.getNickname() : "";
            String subTitle = "";
            switch (mMessage.getFollowType()) {
                case 0:
                    subTitle = getString(R.string.attention_notify_subTitle, nickname);
                    break;
                case 1:
                    subTitle = getString(R.string.cancel_attention_notify_subTitle, nickname);
                    break;
            }
            return subTitle;
        }

        @Override
        protected int getIcon() {
            return R.drawable.ic_follow_remind;
        }

        @Override
        public void onClick() {
            UILauncher.launchAttentionListUI(mContext);
            if (mState == SystemPush.STATE_UNPROCESSED) {
                PushMessageManager.getInstance(mContext).updatePushMessageProcessed(mMsgId);
                mState = SystemPush.STATE_PROCESSED;
            }
        }
    }

    public static class PhotoNotifyItem extends MessageListItem.AbsSystemItem {

        private PhotoNotifyMessage mMessage;

        public PhotoNotifyItem(Context context, SystemPush systemPush) {
            super(context, systemPush);
            mMessage = PhotoNotifyMessage.build(systemPush);
        }

        @Override
        protected String getTitle() {
            if (mMessage != null) {
                String title;
                switch (mMessage.getNotifyType()) {
                    case PhotoNotifyMessage.TYPE_HAS_SEEN:
                        title = getString(R.string.photo_notify_title);
                        break;
                    case PhotoNotifyMessage.TYPE_UPLOAD_MORE:
                        title = getString(R.string.photo_request_title);
                        break;
                    case PhotoNotifyMessage.TYPE_HAS_PRAISE:
                        title = getString(R.string.photo_praise);
                        break;
                    default:
                        title = null;
                        break;
                }
                return title;
            } else {
                return null;
            }
        }

        @Override
        protected String getSubTitle() {
            if (mMessage != null) {
                LiteStranger user = mMessage.getNotifyUser();
                String nickName = user != null ? user.getNickname() : "";
                String subTitle;

                switch (mMessage.getNotifyType()) {
                    case PhotoNotifyMessage.TYPE_HAS_SEEN:
                        subTitle = getString(R.string.someone_saw_the_photo, nickName);
                        break;
                    case PhotoNotifyMessage.TYPE_UPLOAD_MORE:
                        subTitle = getString(R.string.someone_remind_upload_more, nickName);
                        break;
                    case PhotoNotifyMessage.TYPE_HAS_PRAISE:
                        subTitle = getString(R.string.someone_praise_the_photo, nickName);
                        break;
                    default:
                        subTitle = null;
                        break;
                }
                return subTitle;
            } else {
                return null;
            }
        }

        @Override
        protected int getIcon() {
            int resId = R.drawable.ic_app_team;

            if (mMessage != null) {
                switch (mMessage.getNotifyType()) {

                    case PhotoNotifyMessage.TYPE_HAS_SEEN:
                        resId = R.drawable.ic_msg_photo_saw;
                        break;
                    case PhotoNotifyMessage.TYPE_UPLOAD_MORE:
                        resId = R.drawable.ic_msg_photo_remind;
                        break;
                    case PhotoNotifyMessage.TYPE_HAS_PRAISE:
                        resId = R.drawable.ic_praise_remind;
                        break;
                    default:
                        break;
                }
            }
            return resId;
        }

        @Override
        public void onClick() {
            UILauncher.launchPhotoNotifyUI(mContext, mPushType);
        }
    }

    public static class BeenInvitedItem extends MessageListItem.AbsSystemItem {

        private int mState;
        private BeenInvitedMessage mMessage;
        private LiteStranger mStranger;


        public BeenInvitedItem(Context context, SystemPush systemPush) {
            super(context, systemPush);
            mMessage = BeenInvitedMessage.build(systemPush);
            mState = systemPush.getState();
            mStranger = mMessage.getStranger();
        }

        @Override
        protected String getTitle() {
            return getString(R.string.invitation);
        }

        @Override
        protected String getSubTitle() {
            return getString(R.string.new_invite_message, mStranger.getNickname());
        }

        @Override
        protected int getIcon() {
            return R.drawable.ic_invite_receive;
        }

        @Override
        public void onClick() {
            if (mMessage != null) {
                UILauncher.launchInviteUserUI(mContext);
            }
            if (mState == SystemPush.STATE_UNPROCESSED) {
                PushMessageManager.getInstance(mContext).updatePushMessageProcessed(mMsgId);
                mState = SystemPush.STATE_PROCESSED;
            }
        }
    }

    public static class ConfideMessageItem extends MessageListItem.AbsSystemItem {
        private ConfideMessage message;
        private int mState;

        public ConfideMessageItem(Context context, SystemPush systemPush) {
            super(context, systemPush);
            mState = systemPush.getState();
            message = ConfideMessage.build(systemPush);
        }

        @Override
        protected String getTitle() {
            if (message != null) {
                String title;
                switch (message.getOperateType()) {
                    case ConfideMessage.TYPE_OPERATE_PRAISE:
                        title = getString(R.string.confide_praise_title);
                        break;
                    case ConfideMessage.TYPE_OPERATE_COMMENT:
                        title = getString(R.string.confide_comment_title);
                        break;
                    default:
                        title = null;
                        break;
                }
                return title;
            } else {
                return null;
            }
        }

        @Override
        protected String getSubTitle() {
            if (message != null) {
                Stranger stranger = message.getStranger();
                String nickname = stranger != null ? stranger.getNickname() : "";
                String subTitle;
                switch (message.getOperateType()) {
                    case ConfideMessage.TYPE_OPERATE_PRAISE:
                        subTitle = getString(R.string.confide_praise, nickname);
                        break;
                    case ConfideMessage.TYPE_OPERATE_COMMENT:
                        subTitle = TextUtils.isEmpty(message.getReplyCommentContent()) ?
                                getString(R.string.confide_comment, nickname) :
                                getString(R.string.confide_reply_comment, nickname);
                        break;
                    default:
                        subTitle = null;
                        break;
                }
                return subTitle;
            } else {
                return null;
            }
        }

        @Override
        protected int getIcon() {
            int resId = R.drawable.ic_app_team;
            if (message != null) {
                switch (message.getOperateType()) {
                    case ConfideMessage.TYPE_OPERATE_PRAISE:
                        resId = R.drawable.ic_confide_praise;
                        break;
                    case ConfideMessage.TYPE_OPERATE_COMMENT:
                        resId = R.drawable.ic_confide_comment;
                        break;
                }
            }
            return resId;
        }

        @Override
        public void onClick() {
            UILauncher.launchConfideNotifyUI(mContext, mPushType);
        }
    }
}

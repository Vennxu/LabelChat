package com.ekuater.labelchat.ui.fragment.main;

import android.content.Context;
import android.text.TextUtils;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.push.CommentPush;
import com.ekuater.labelchat.ui.fragment.push.PraisePush;
import com.ekuater.labelchat.ui.fragment.push.RemaindPush;
import com.ekuater.labelchat.ui.fragment.push.SystemPushUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;

/**
 * Created by Administrator on 2015/5/5.
 *
 * @author Xu Wenxiang
 */
public class SystemGroupItem {

    public static class CommentItem extends MessageListItem.AbsSystemGroupMessage {

        private SystemPush mSystemPush;
        private CommentPush commentPush;

        public CommentItem(Context context, SystemPush systemPush) {
            super(context, systemPush);
            mSystemPush = systemPush;
            commentPush = SystemPushUtils.getCommentJsonContent(mSystemPush);
        }

        @Override
        protected String getTitle() {
            return getString(R.string.labelstory_item_comment);
        }

        @Override
        protected String getSubTitle() {
            String subTitle = null;
            if (commentPush != null) {
                Stranger stranger = commentPush.getStranger();
                String title = "";
                if (stranger != null) {
                    title = MiscUtils.getUserRemarkName(mContext, stranger.getUserId());
                }
                String nickname = title != null && title.length() > 0 ? title : stranger != null ? stranger.getNickname() : "";
                if (CommentPush.DYNAMIC.equals(commentPush.getChildType())) {
                    subTitle = TextUtils.isEmpty(commentPush.getReply())
                            ? getSub(commentPush.getFlag(), nickname)
                            : getString(R.string.someone_reply_your_comment, nickname);
                } else if (CommentPush.CONFIDE.equals(commentPush.getChildType())) {
                    subTitle = TextUtils.isEmpty(commentPush.getReply()) ?
                            getString(R.string.confide_comment, nickname) :
                            getString(R.string.confide_reply_comment, nickname);
                } else if (CommentPush.CONFIDE.equals(commentPush.getChildType())) {
                    subTitle = getString(R.string.someone_praise_the_photo, nickname);
                }
            }
            return subTitle;
        }

        @Override
        protected int getIcon() {
            return R.drawable.ic_story_message;
        }

        @Override
        public void onClick() {
            if (commentPush != null && mContext != null) {
                UILauncher.launchSystemNotifyUI(mContext, commentPush.getGroupType());
            }
        }

        private String getSub(String type, String nickname) {
            String subTitle;

            switch (type) {
                case LabelStory.TYPE_TXT_IMG:
                    subTitle = getString(R.string.image_comment, nickname);
                    break;
                case LabelStory.TYPE_ONLINEAUDIO:
                case LabelStory.TYPE_AUDIO:
                    subTitle = getString(R.string.voice_comment, nickname);
                    break;
                case LabelStory.TYPE_BANKNOTE:
                    subTitle = getString(R.string.banknote_comment, nickname);
                    break;
                default:
                    subTitle = null;
                    break;
            }
            return subTitle;
        }
    }

    public static class PraiseItem extends MessageListItem.AbsSystemGroupMessage {

        private PraisePush praisePush;

        public PraiseItem(Context context, SystemPush systemPush) {
            super(context, systemPush);
            praisePush = SystemPushUtils.getPraiseJsonContent(systemPush);
        }

        @Override
        protected String getTitle() {
            return getString(R.string.labelstory_item_click_priase);
        }

        @Override
        protected String getSubTitle() {
            String subTitle = null;
            if (praisePush != null) {
                Stranger stranger = praisePush.getStranger();
                String title = "";
                if (stranger != null) {
                    title = MiscUtils.getUserRemarkName(mContext, stranger.getUserId());
                }
                String nickname = title != null && title.length() > 0 ? title : stranger != null ? stranger.getNickname() : "";
                if (PraisePush.DYNAMIC.equals(praisePush.getChildType())) {
                    subTitle = getSubtitle(praisePush.getFlag(), nickname);
                } else if (PraisePush.CONFIDE.equals(praisePush.getChildType())) {
                    subTitle = getString(R.string.confide_praise, nickname);
                } else if (PraisePush.PHOTO.equals(praisePush.getChildType())) {
                    subTitle = getString(R.string.someone_praise_the_photo, nickname);
                }
            }
            return subTitle;
        }

        private String getSubtitle(String type, String nickname) {
            String subTitle;

            switch (type) {
                case LabelStory.TYPE_TXT_IMG:
                    subTitle = getString(R.string.image_praise, nickname);
                    break;
                case LabelStory.TYPE_ONLINEAUDIO:
                case LabelStory.TYPE_AUDIO:
                    subTitle = getString(R.string.voice_praise, nickname);
                    break;
                case LabelStory.TYPE_BANKNOTE:
                    subTitle = getString(R.string.banknote_praise, nickname);
                    break;
                default:
                    subTitle = null;
                    break;
            }
            return subTitle;
        }

        @Override
        protected int getIcon() {
            return R.drawable.ic_praise_remind;
        }

        @Override
        public void onClick() {
            if (praisePush != null) {
                UILauncher.launchSystemNotifyUI(mContext, praisePush.getGroupType());
            }
        }
    }

    public static class RemaindItem extends MessageListItem.AbsSystemGroupMessage {

        private RemaindPush remaindPush;

        public RemaindItem(Context context, SystemPush systemPush) {
            super(context, systemPush);
            remaindPush = SystemPushUtils.getRemaindJsonContent(systemPush);
        }

        @Override
        protected String getTitle() {
            return getString(R.string.photo_notify_title);
        }

        @Override
        protected String getSubTitle() {
            String subTitle = null;
            if (remaindPush != null) {
                Stranger stranger = remaindPush.getStranger();
                String title = "";
                if (stranger != null) {
                    title = MiscUtils.getUserRemarkName(mContext, stranger.getUserId());
                }
                String nickName = title != null && title.length() > 0 ? title : stranger != null ? stranger.getNickname() : "";
                switch (remaindPush.getRemindType()) {
                    case RemaindPush.FOLLOW:
                        switch (remaindPush.getRemindFlag()) {
                            case 0:
                                subTitle = getString(R.string.attention_notify_subTitle, nickName);
                                break;
                            case 1:
                                subTitle = getString(R.string.cancel_attention_notify_subTitle, nickName);
                                break;
                        }
                        break;
                    case RemaindPush.INVITED:
                        subTitle = getString(R.string.new_invite_message, nickName);
                        break;
                    case RemaindPush.PHOTO:
                        switch (remaindPush.getRemindFlag()) {
                            case 1:
                                subTitle = getString(R.string.someone_saw_the_photo, nickName);
                                break;
                            case 2:
                                subTitle = getString(R.string.someone_remind_upload_more, nickName);
                                break;
                        }
                        break;
                    case RemaindPush.NEW_PHOTO:
                        subTitle = getString(R.string.new_photo, nickName);
                        break;
                    case RemaindPush.INTERACT:
                        if (remaindPush.getRemindFlag() == 1) {
                            subTitle = getString(R.string.interact_tag, nickName);
                        } else {
                            subTitle = getString(R.string.interact_interest, nickName);
                        }
                        break;
                    case RemaindPush.CONFIDE_RECOMMEND:
                        subTitle = getString(R.string.confide_recommend);
                        break;
                    case RemaindPush.INTEREST:
                        subTitle = getString(R.string.interest_remaind_message_name, nickName, remaindPush.getFlag());
                        break;
                    case RemaindPush.DYNAMIC:
                        switch (remaindPush.getFlag()) {
                            case LabelStory.TYPE_ONLINEAUDIO:
                            case LabelStory.TYPE_AUDIO:
                                subTitle = getString(R.string.voice_remaind_message_name, nickName);
                                break;
                            case LabelStory.TYPE_TXT_IMG:
                                subTitle = getString(R.string.image_remaind_message_name, nickName);
                                break;
                            case LabelStory.TYPE_BANKNOTE:
                                subTitle = getString(R.string.banknote_remaind_message_name, nickName);
                                break;
                            default:
                                break;
                        }
                        break;
                    case RemaindPush.USERTAG:
                        subTitle = getString(R.string.usertag_remaind_message_name, nickName);
                        break;
                }
            }
            return subTitle;
        }

        @Override
        protected int getIcon() {
            return R.drawable.ic_msg_photo_saw;
        }

        @Override
        public void onClick() {
            if (remaindPush != null) {
                UILauncher.launchSystemNotifyUI(mContext, remaindPush.getGroupType());
            }
        }
    }
}

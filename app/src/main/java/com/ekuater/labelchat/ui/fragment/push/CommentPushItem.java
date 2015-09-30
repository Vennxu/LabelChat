package com.ekuater.labelchat.ui.fragment.push;

import android.content.Context;
import android.support.v4.util.SparseArrayCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.datastruct.Confide;
import com.ekuater.labelchat.datastruct.ConfideMessage;
import com.ekuater.labelchat.datastruct.DynamicOperateMessage;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.LiteStranger;
import com.ekuater.labelchat.datastruct.PhotoNotifyMessage;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.SystemPushType;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.delegate.AlbumManager;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ConfideManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.SimpleProgressHelper;
import com.ekuater.labelchat.ui.fragment.friends.StrangerHelper;
import com.ekuater.labelchat.ui.fragment.labelstory.DynamicArguments;
import com.ekuater.labelchat.ui.fragment.labelstory.DynamicDetailsHelper;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;
import com.ekuater.labelchat.util.TextUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;

/**
 * Created by Administrator on 2015/5/5.
 */
public class CommentPushItem {

    private static final SparseArrayCompat<Class<? extends SystemPushListItem.CommentAbsPushItem>> sItemMap;

    static {
        sItemMap = new SparseArrayCompat<>();
        sItemMap.put(SystemPushType.TYPE_LABEL_STORY_COMMENTS, ImageCommentItem.class);
        sItemMap.put(SystemPushType.TYPE_CONFIDE_COMMEND, ConfideCommentItem.class);
    }

    public static SystemPushListItem.CommentAbsPushItem build(Context context, SimpleProgressHelper simpleProgressHelper, StrangerHelper strangerHelper, AvatarManager avatarManager, SystemPush systemPush) {
        if (systemPush == null) {
            throw new NullPointerException("Build AbsSystemItem empty system push");
        }

        try {
            Class<?> clazz = sItemMap.get(systemPush.getType());
            Constructor<?> constructor = clazz.getConstructor(Context.class, SimpleProgressHelper.class, StrangerHelper.class, AvatarManager.class, SystemPush.class);
            return (SystemPushListItem.CommentAbsPushItem) constructor.newInstance(context, simpleProgressHelper, strangerHelper, avatarManager, systemPush);
        } catch (Exception e) {
            return null;
        }
    }

    public static class ImageCommentItem extends SystemPushListItem.CommentAbsPushItem {

        private Context mContext;
        private DynamicOperateMessage dynamic;
        private SimpleProgressHelper simpleProgressHelper;
        private AvatarManager avatarManager;
        private StrangerHelper strangerHelper;


        public ImageCommentItem(Context context, SimpleProgressHelper simpleProgressHelper, StrangerHelper strangerHelper, AvatarManager avatarManager, SystemPush systemPush) {
            super(context, systemPush);
            mContext = context;
            dynamic = DynamicOperateMessage.build(systemPush);
            this.simpleProgressHelper = simpleProgressHelper;
            this.avatarManager = avatarManager;
            this.strangerHelper = strangerHelper;
        }

        @Override
        protected void getTitle(TextView textView) {

            if (dynamic != null) {
                Stranger stranger = dynamic.getStranger();
                if (stranger != null) {
                    String title = MiscUtils.getUserRemarkName(mContext, stranger.getUserId());
                    textView.setText(title != null && title.length() > 0 ? title : stranger.getNickname());
                }
            } else {
                textView.setText(getString(R.string.unknown));
            }
        }

        @Override
        protected void getSubTitle(TextView title) {
            if (TextUtils.isEmpty(dynamic.getReplyDynamicCommentContent())) {
                title.setText(dynamic.getDynamicCommentContent());
            } else {
                String commentContent = " @" + dynamic.getReplyNickname() + getString(R.string.colon);
                String content = dynamic.getReplyDynamicCommentContent() + commentContent + dynamic.getDynamicCommentContent();
                int end = dynamic.getReplyDynamicCommentContent().length() + commentContent.length();
                SpannableString ss = new SpannableString(content);
                ForegroundColorSpan redSpan = new ForegroundColorSpan(getColor(R.color.comment_name));
                ForegroundColorSpan whiteSpan = new ForegroundColorSpan(getColor(R.color.story_time));
                ss.setSpan(redSpan, dynamic.getReplyDynamicCommentContent().length(), end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ss.setSpan(whiteSpan, 0, dynamic.getReplyDynamicCommentContent().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                title.setText(ss);
            }
        }

        @Override
        protected void setIcon(CircleImageView circleImageView) {
            if (dynamic != null && dynamic.getStranger() != null) {
                MiscUtils.showAvatarThumb(avatarManager, dynamic.getStranger().getAvatarThumb(), circleImageView, R.drawable.contact_single);
                circleImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        strangerHelper.showStranger(dynamic.getStranger().getUserId());
                    }
                });
            }
        }

        @Override
        protected void setImage(ImageView image) {

            if (dynamic != null) {
                switch (dynamic.getDynamicType()) {
                    case LabelStory.TYPE_BANKNOTE:
                    case LabelStory.TYPE_TXT_IMG:
                        if (dynamic.getDynamicImgThumb() != null) {
                            image.setVisibility(View.VISIBLE);
                            MiscUtils.showLabelStoryCommentAvatarThumb(avatarManager, dynamic.getDynamicImgThumb().split(";")[0], image, R.drawable.image_load_fail);
                        } else {
                            image.setVisibility(View.GONE);
                        }
                        break;
                    case LabelStory.TYPE_AUDIO:
                        image.setVisibility(View.VISIBLE);
                        image.setImageResource(R.drawable.sound_play_bg);
                        break;
                    case LabelStory.TYPE_ONLINEAUDIO:
                        image.setVisibility(View.VISIBLE);
                        if (dynamic.getDynamicImgThumb() != null) {
                            avatarManager.displaySingerAvatar(dynamic.getDynamicImgThumb().split(";")[0], image, R.drawable.sound_play_bg);
                        }else{
                            image.setImageResource(R.drawable.sound_play_bg);
                        }
                        break;
                    default:
                        image.setVisibility(View.GONE);
                        break;
                }
            }

        }

        @Override
        protected void setContent(TextView textView) {
            if (dynamic != null) {
                switch (dynamic.getDynamicType()) {
                    case LabelStory.TYPE_AUDIO:
                        if (TextUtil.isEmpty(dynamic.getDynamicContent())) {
                            textView.setText(dynamic.getDynamicContent());
                        } else {
                            textView.setVisibility(View.GONE);
                        }
                        break;
                    default:
                        textView.setVisibility(View.GONE);
                        break;
                }
            }
        }

        @Override
        protected void setTitleContent(TextView titleContent) {
            if (dynamic != null) {
                switch (dynamic.getDynamicType()) {
                    case LabelStory.TYPE_AUDIO:
                        titleContent.setVisibility(View.VISIBLE);
                        titleContent.setText(dynamic.getDynamicContent());
                        break;
                    case LabelStory.TYPE_ONLINEAUDIO:
                        titleContent.setVisibility(View.VISIBLE);
                        if (!TextUtil.isEmpty(dynamic.getDynamicContent())) {
                            try {
                                JSONObject json = new JSONObject(dynamic.getDynamicContent());
                                titleContent.setText(json.getString(CommandFields.Dynamic.DYNAMIC_CONTENT));
                            } catch (Exception e) {
                                titleContent.setVisibility(View.GONE);
                            }
                        } else {
                            titleContent.setVisibility(View.GONE);
                        }
                        break;
                    default:
                        titleContent.setVisibility(View.GONE);
                        break;
                }
            }
        }

        @Override
        public void onClick() {
            if (dynamic != null) {
                changetState();
                LabelStory labelStory = new LabelStory();
                labelStory.setLabelStoryId(dynamic.getDynamicId());
                DynamicArguments arguments = new DynamicArguments();
                arguments.setLabelStory(labelStory);
                arguments.setIsShowTitle(true);
                arguments.setIsShowFragment(true);
                arguments.setIsComment(true);
                DynamicDetailsHelper dynamicDetailsHelper = new DynamicDetailsHelper(getContext(), simpleProgressHelper, arguments);
                dynamicDetailsHelper.loadDynamicDetails();
            }
        }
    }

    public static class ConfideCommentItem extends SystemPushListItem.CommentAbsPushItem {

        private ConfideMessage confide;
        private SimpleProgressHelper simpleProgressHelper;
        private AvatarManager avatarManager;
        private StrangerHelper strangerHelper;

        public ConfideCommentItem(Context context, SimpleProgressHelper simpleProgressHelper, StrangerHelper strangerHelper, AvatarManager avatarManager, SystemPush systemPush) {
            super(context, systemPush);
            confide = ConfideMessage.build(systemPush);
            this.simpleProgressHelper = simpleProgressHelper;
            this.avatarManager = avatarManager;
            this.strangerHelper = strangerHelper;
        }

        public static int sp2px(Context context, float spValue) {
            final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
            return (int) (spValue * fontScale + 0.5f);
        }

        @Override
        protected void getTitle(TextView textView) {
            if (TextUtils.isEmpty(confide.getReplyCommentContent())) {
                textView.setText(getString(R.string.confide_floor, confide.getFloor()));
            } else {
                String commentContent = "@" + confide.getReplyFloor() + getString(R.string.labelstory_item_floor) + getString(R.string.colon);
                String content = commentContent + confide.getReplyCommentContent();
                int end = confide.getReplyCommentContent().length() + commentContent.length();
                SpannableString ss = new SpannableString(content);

                ForegroundColorSpan redSpan = new ForegroundColorSpan(getColor(R.color.comment_name));
                ForegroundColorSpan whiteSpan = new ForegroundColorSpan(getColor(R.color.story_time));
                ss.setSpan(whiteSpan, commentContent.length(), end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ss.setSpan(new AbsoluteSizeSpan(sp2px(getContext(), 14)), commentContent.length(), end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                textView.setText(ss);
            }
        }

        @Override
        protected void getSubTitle(TextView textView) {
            textView.setText(confide.getCommentContent());
        }

        @Override
        protected void setIcon(CircleImageView circleImageView) {
            if (confide != null) {
                if (confide.getStranger() != null) {
                    MiscUtils.showAvatarThumb(avatarManager, confide.getStranger().getAvatarThumb(), circleImageView, R.drawable.contact_single);
                } else {
                    MiscUtils.showConfideAvatarThumb(avatarManager, confide.getVirtualAvatar(), circleImageView, R.drawable.contact_single);
                }
                circleImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        strangerHelper.showStranger(confide.getStranger().getUserId());
                    }
                });
            }
        }

        @Override
        protected void setImage(ImageView image) {
            image.setVisibility(View.GONE);
        }

        @Override
        protected void setContent(TextView textView) {
            if (confide != null && confide.getConfide() != null) {
                textView.setVisibility(View.VISIBLE);
                textView.setText(confide.getConfide().getConfideContent());
                if (TextUtil.isEmpty(confide.getConfide().getConfideBgImg())) {
                    textView.setBackgroundColor(confide.getConfide().parseBgColor());
                }else{
                    textView.setBackgroundResource(ConfideManager.getInstance(getContext()).getConfideBs().get(confide.getConfide().getConfideBgImg()));
                }
            }
        }

        @Override
        protected void setTitleContent(TextView titleContent) {
            titleContent.setVisibility(View.GONE);
        }

        @Override
        public void onClick() {
            if (confide != null) {
                changetState();
                Confide confides = confide.getConfide();
                UILauncher.launchConfideDetaileUI(getContext(), confides, 0);
            }
        }
    }
}

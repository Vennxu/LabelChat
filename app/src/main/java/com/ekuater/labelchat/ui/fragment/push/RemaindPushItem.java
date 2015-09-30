package com.ekuater.labelchat.ui.fragment.push;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.util.SparseArrayCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.datastruct.FollowUser;
import com.ekuater.labelchat.datastruct.LiteStranger;
import com.ekuater.labelchat.datastruct.UserTag;
import com.ekuater.labelchat.datastruct.UserTagMessage;
import com.ekuater.labelchat.datastruct.AlbumPhoto;
import com.ekuater.labelchat.datastruct.BeenFollowedMessage;
import com.ekuater.labelchat.datastruct.BeenInvitedMessage;
import com.ekuater.labelchat.datastruct.ConfideMessage;
import com.ekuater.labelchat.datastruct.DynamicRemaindMessage;
import com.ekuater.labelchat.datastruct.InteractMessage;
import com.ekuater.labelchat.datastruct.InterestMessage;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.PhotoNotifyMessage;
import com.ekuater.labelchat.datastruct.PushInteract;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.SystemPushType;
import com.ekuater.labelchat.datastruct.UserInterest;
import com.ekuater.labelchat.delegate.AlbumManager;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ConfideManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.SimpleProgressHelper;
import com.ekuater.labelchat.ui.fragment.friends.StrangerHelper;
import com.ekuater.labelchat.ui.fragment.labelstory.DynamicArguments;
import com.ekuater.labelchat.ui.fragment.labelstory.DynamicDetailsHelper;
import com.ekuater.labelchat.util.InterestUtils;
import com.ekuater.labelchat.ui.util.CompatUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.widget.CircleImageView;
import com.ekuater.labelchat.util.TextUtil;

import org.json.JSONObject;

import java.lang.reflect.Constructor;

/**
 * Created by Administrator on 2015/5/5.
 *
 * @author Xu WenXiang
 */
public class RemaindPushItem {

    private static final SparseArrayCompat<Class<? extends SystemPushListItem.RemaindAbsPushItem>> sItemMap;

    static {
        sItemMap = new SparseArrayCompat<>();
        sItemMap.put(SystemPushType.TYPE_BEEN_FOLLOWED, FollowItem.class);
        sItemMap.put(SystemPushType.TYPE_PHOTO_NOTIFY, PhotoItem.class);
        sItemMap.put(SystemPushType.TYPE_BEEN_INVITED, InvitedItem.class);
        sItemMap.put(SystemPushType.TYPE_UPLOAD_PHOTO, UploadPhotoItem.class);
        sItemMap.put(SystemPushType.TYPE_TAG_INTERACT, InteractItem.class);
        sItemMap.put(SystemPushType.TYPE_CONFIDE_RECOMMEND, ConfideRecommendItem.class);
        sItemMap.put(SystemPushType.TYPE_REMAIND_INTEREST, InterestItem.class);
        sItemMap.put(SystemPushType.TYPE_REMAIND_DYNAMIC, DynamicItem.class);
        sItemMap.put(SystemPushType.TYPE_REMAIND_TAG, UserTagItem.class);
    }

    public static SystemPushListItem.RemaindAbsPushItem build(
            Context context, SimpleProgressHelper simpleProgressHelper,
            StrangerHelper strangerHelper, AvatarManager avatarManager,
            SystemPush systemPush) {
        if (systemPush == null) {
            throw new NullPointerException("Build AbsSystemItem empty system push");
        }

        try {
            Class<?> clazz = sItemMap.get(systemPush.getType());
            Constructor<?> constructor = clazz.getConstructor(Context.class,
                    SimpleProgressHelper.class, StrangerHelper.class,
                    AvatarManager.class, SystemPush.class);
            return (SystemPushListItem.RemaindAbsPushItem) constructor.newInstance(context,
                    simpleProgressHelper, strangerHelper, avatarManager, systemPush);
        } catch (Exception e) {
            return null;
        }
    }

    public static class FollowItem extends SystemPushListItem.RemaindAbsPushItem {

        private Context mContext;
        private BeenFollowedMessage followedMessage;
        private AvatarManager avatarManager;
        private StrangerHelper strangerHelper;

        public FollowItem(Context context, SimpleProgressHelper simpleProgressHelper,
                          StrangerHelper strangerHelper, AvatarManager avatarManager,
                          SystemPush systemPush) {
            super(context, systemPush);
            mContext = context;
            followedMessage = BeenFollowedMessage.build(systemPush);
            this.avatarManager = avatarManager;
            this.strangerHelper = strangerHelper;
        }

        @Override
        protected String getTitle() {
            if (followedMessage != null) {
                FollowUser followUser = followedMessage.getFollowUser();
                if (followUser != null) {
                    String title = MiscUtils.getUserRemarkName(mContext, followUser.getUserId());
                    return title != null && title.length() > 0 ? title : followUser.getNickname();
                }
            }
            return getString(R.string.unknown);

        }

        @Override
        protected String getSubTitle() {
            String subTitle = null;
            if (followedMessage != null) {
                switch (followedMessage.getFollowType()) {
                    case BeenFollowedMessage.TYPE_FOLLOWED:
                        subTitle = getString(R.string.attention_message);
                        break;
                    case BeenFollowedMessage.TYPE_CANCEL_FOLLOW:
                        subTitle = getString(R.string.cancel_attention_message);
                        break;
                    default:
                        subTitle = null;
                        break;
                }
            }
            return subTitle;
        }

        @Override
        protected void setIcon(CircleImageView circleImageView) {
            if (followedMessage != null && followedMessage.getFollowUser() != null) {
                MiscUtils.showAvatarThumb(avatarManager,
                        followedMessage.getFollowUser().getAvatarThumb(),
                        circleImageView, R.drawable.contact_single);
                circleImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        strangerHelper.showStranger(followedMessage.getFollowUser().getUserId());
                    }
                });

            }
        }

        @Override
        protected void setImage(ImageView image) {
            image.setVisibility(View.GONE);
        }

        @Override
        public void onClick() {
            changetState();
            if (followedMessage != null && followedMessage.getFollowUser() != null) {
                strangerHelper.showStranger(followedMessage.getFollowUser().getUserId());
            }
        }

        @Override
        protected void setRightTitle(TextView rightTitle) {
            rightTitle.setVisibility(View.GONE);
        }

        @Override
        protected void setConfideContent(TextView textView) {
            textView.setVisibility(View.GONE);
        }

        @Override
        protected void setVoiceContent(TextView textView) {
            textView.setVisibility(View.GONE);
        }
    }

    public static class PhotoItem extends SystemPushListItem.RemaindAbsPushItem {

        private Context mContext;
        private PhotoNotifyMessage photo;
        private AvatarManager avatarManager;
        private AlbumManager albumManager;
        private LayoutInflater inflater;
        private StrangerHelper strangerHelper;


        public PhotoItem(Context context, SimpleProgressHelper simpleProgressHelper,
                         StrangerHelper strangerHelper, AvatarManager avatarManager,
                         SystemPush systemPush) {
            super(context, systemPush);
            mContext = context;
            photo = PhotoNotifyMessage.build(systemPush);
            this.avatarManager = avatarManager;
            this.strangerHelper = strangerHelper;
            inflater = LayoutInflater.from(context);
            albumManager = AlbumManager.getInstance(context);

        }

        @Override
        protected String getTitle() {
            if (photo != null) {
                LiteStranger stranger = photo.getNotifyUser();
                if (stranger != null) {
                    String title = MiscUtils.getUserRemarkName(mContext, stranger.getUserId());
                    return title != null && title.length() > 0 ? title : stranger.getNickname();
                }
            }
            return getString(R.string.unknown);
        }

        @Override
        protected String getSubTitle() {
            String subTitle = null;
            if (photo != null) {
                switch (photo.getNotifyType()) {
                    case PhotoNotifyMessage.TYPE_HAS_SEEN:
                        subTitle = getString(R.string.saw_the_photo);
                        break;
                    case PhotoNotifyMessage.TYPE_UPLOAD_MORE:
                        subTitle = getString(R.string.remind_upload_more);
                        break;
                    default:
                        subTitle = null;
                        break;
                }
            }
            return subTitle;
        }

        @Override
        protected void setIcon(CircleImageView circleImageView) {
            if (photo != null && photo.getNotifyUser() != null) {
                MiscUtils.showAvatarThumb(avatarManager, photo.getNotifyUser().getAvatarThumb(),
                        circleImageView, R.drawable.contact_single);
                circleImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        strangerHelper.showStranger(photo.getNotifyUser().getUserId());
                    }
                });
            }
        }

        @Override
        protected void setImage(ImageView image) {
            if (photo != null && photo.getAlbumPhoto() != null) {
                image.setVisibility(View.VISIBLE);
                albumManager.displayPhotoThumb(photo.getAlbumPhoto().getPhotoThumb(),
                        image, R.drawable.pic_loading);
            }
        }

        @Override
        public void onClick() {
            if (photo != null) {
                changetState();
                AlbumPhoto albumPhoto = photo.getAlbumPhoto();
                UILauncher.launchMyAlbumGalleryUI(getContext(), new AlbumPhoto[]{albumPhoto}, 0);
            }
        }

        @Override
        protected void setRightTitle(TextView rightTitle) {
            rightTitle.setVisibility(View.GONE);
        }

        @Override
        protected void setConfideContent(TextView textView) {
            textView.setVisibility(View.GONE);
        }

        @Override
        protected void setVoiceContent(TextView textView) {
            textView.setVisibility(View.GONE);
        }
    }

    public static class InvitedItem extends SystemPushListItem.RemaindAbsPushItem {

        private Context mContext;
        private BeenInvitedMessage invite;
        private AvatarManager avatarManager;
        private StrangerHelper strangerHelper;


        public InvitedItem(Context context, SimpleProgressHelper simpleProgressHelper,
                           StrangerHelper strangerHelper, AvatarManager avatarManager,
                           SystemPush systemPush) {
            super(context, systemPush);
            mContext = context;
            invite = BeenInvitedMessage.build(systemPush);
            this.avatarManager = avatarManager;
            this.strangerHelper = strangerHelper;

        }

        @Override
        protected String getTitle() {
            if (invite != null) {
                LiteStranger stranger = invite.getStranger();
                if (stranger != null) {
                    String title = MiscUtils.getUserRemarkName(mContext, stranger.getUserId());
                    return title != null && title.length() > 0 ? title : stranger.getNickname();
                }
            }
            return getString(R.string.unknown);
        }

        @Override
        protected String getSubTitle() {
            return getString(R.string.invite_message);
        }

        @Override
        protected void setIcon(CircleImageView circleImageView) {
            if (invite != null && invite.getStranger() != null) {
                MiscUtils.showAvatarThumb(avatarManager, invite.getStranger().getAvatarThumb(), circleImageView, R.drawable.contact_single);
                circleImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        strangerHelper.showStranger(invite.getStranger().getUserId());
                    }
                });
            }
        }

        @Override
        protected void setImage(ImageView image) {
            image.setVisibility(View.GONE);
        }

        @Override
        public void onClick() {
            changetState();
            if (invite != null && invite.getStranger() != null) {
                strangerHelper.showStranger(invite.getStranger().getUserId());
            }
        }

        @Override
        protected void setRightTitle(TextView rightTitle) {
            rightTitle.setVisibility(View.GONE);
        }

        @Override
        protected void setConfideContent(TextView textView) {
            textView.setVisibility(View.GONE);
        }

        @Override
        protected void setVoiceContent(TextView textView) {
            textView.setVisibility(View.GONE);
        }
    }

    public static class UploadPhotoItem extends SystemPushListItem.RemaindAbsPushItem {
        private Context mContext;
        private PhotoNotifyMessage uploadMessage;
        private AvatarManager avatarManager;
        private AlbumManager albumManager;
        private StrangerHelper strangerHelper;


        public UploadPhotoItem(Context context, SimpleProgressHelper simpleProgressHelper,
                               StrangerHelper strangerHelper, AvatarManager avatarManager,
                               SystemPush systemPush) {
            super(context, systemPush);
            mContext = context;
            uploadMessage = PhotoNotifyMessage.build(systemPush);
            this.avatarManager = avatarManager;
            this.strangerHelper = strangerHelper;
            albumManager = AlbumManager.getInstance(context);

        }

        @Override
        protected String getTitle() {
            if (uploadMessage != null) {
                LiteStranger stranger = uploadMessage.getNotifyUser();
                if (stranger != null) {
                    String title = MiscUtils.getUserRemarkName(mContext, stranger.getUserId());
                    return title != null && title.length() > 0 ? title : stranger.getNickname();
                }
            }
            return getString(R.string.unknown);
        }

        @Override
        protected String getSubTitle() {
            return getString(R.string.new_photo_message);
        }

        @Override
        protected void setIcon(CircleImageView circleImageView) {
            if (uploadMessage != null && uploadMessage.getNotifyUser() != null) {
                MiscUtils.showAvatarThumb(avatarManager, uploadMessage.getNotifyUser().getAvatarThumb(), circleImageView, R.drawable.contact_single);
                circleImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        strangerHelper.showStranger(uploadMessage.getNotifyUser().getUserId());
                    }
                });
            }
        }

        @Override
        protected void setImage(ImageView image) {
            if (uploadMessage != null && uploadMessage.getAlbumPhoto() != null) {
                image.setVisibility(View.VISIBLE);
                albumManager.displayPhotoThumb(uploadMessage.getAlbumPhoto().getPhotoThumb(), image, R.drawable.pic_loading
                );
            }
        }

        @Override
        protected void setRightTitle(TextView rightTitle) {
            rightTitle.setVisibility(View.GONE);
        }

        @Override
        protected void setConfideContent(TextView textView) {
            textView.setVisibility(View.GONE);
        }

        @Override
        protected void setVoiceContent(TextView textView) {
            textView.setVisibility(View.GONE);
        }

        @Override
        public void onClick() {
            if (uploadMessage != null) {
                changetState();
                UILauncher.launchAlbumGalleryUI(getContext(), uploadMessage.getNotifyUser(),
                        uploadMessage.getAlbumPhoto().getPhotoId());
            }
        }
    }

    public static class InteractItem extends SystemPushListItem.RemaindAbsPushItem {
        private Context mContext;
        private InteractMessage interactMessage;
        private AvatarManager avatarManager;
        private StrangerHelper strangerHelper;


        public InteractItem(Context context, SimpleProgressHelper simpleProgressHelper,
                            StrangerHelper strangerHelper, AvatarManager avatarManager,
                            SystemPush systemPush) {
            super(context, systemPush);
            mContext = context;
            interactMessage = InteractMessage.build(systemPush);
            this.avatarManager = avatarManager;
            this.strangerHelper = strangerHelper;

        }

        @Override
        protected String getTitle() {
            if (interactMessage != null && interactMessage.getInteract() != null) {
                PushInteract pushInteract = interactMessage.getInteract();
                LiteStranger stranger = pushInteract.getStranger();
                if (stranger != null) {
                    String title = MiscUtils.getUserRemarkName(mContext, stranger.getUserId());
                    return title != null && title.length() > 0 ? title : stranger.getNickname();
                }
            }
            return getString(R.string.unknown);
        }

        @Override
        protected String getSubTitle() {
            return interactMessage != null && interactMessage.getInteract() != null ?
                    interactMessage.getInteract().getInteractOperate() : null;
        }

        @Override
        protected void setIcon(CircleImageView circleImageView) {
            if (interactMessage != null && interactMessage.getInteract() != null &&
                    interactMessage.getInteract().getStranger() != null) {
                MiscUtils.showAvatarThumb(avatarManager, interactMessage.getInteract().getStranger().getAvatarThumb(),
                        circleImageView, R.drawable.contact_single);
                circleImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        strangerHelper.showStranger(interactMessage.getInteract().getStranger().getUserId());
                    }
                });
            }
        }

        @Override
        protected void setImage(ImageView image) {
            image.setVisibility(View.GONE);
        }

        @Override
        protected void setRightTitle(TextView rightTitle) {
            if (interactMessage != null && interactMessage.getInteract() != null) {
                rightTitle.setVisibility(View.VISIBLE);
                PushInteract interact = interactMessage.getInteract();
                switch (interact.getInteractType()) {
                    case PushInteract.TYPE_INTERACT_TAG:
                        rightTitle.setText(interact.getInteractObject());
                        rightTitle.setTextColor(Color.WHITE);
                        GradientDrawable drawable = (GradientDrawable) getContext().getResources()
                                .getDrawable(R.drawable.corners_bg);
                        if (drawable != null) {
                            drawable.setColor(interact.parseTagColor());
                            CompatUtils.setBackground(rightTitle, drawable);
                        }
                        break;
                    case PushInteract.TYPE_INTERACT_INTEREST:
                        rightTitle.setText(interact.getInteractObject());
                        setColor(Integer.parseInt(interact.getObjectType()), rightTitle);
                        break;
                    default:
                        break;
                }
            }
        }

        @Override
        protected void setConfideContent(TextView textView) {
            textView.setVisibility(View.GONE);
        }

        @Override
        protected void setVoiceContent(TextView textView) {
            textView.setVisibility(View.GONE);
        }

        @Override
        public void onClick() {
            changetState();
            if (interactMessage != null && interactMessage.getInteract() != null &&
                    interactMessage.getInteract().getStranger() != null) {
                strangerHelper.showStranger(interactMessage.getInteract().getStranger().getUserId());
            }
        }

        private void setColor(int type, TextView rightTitle) {
            InterestUtils.setInterestColor(getContext(), rightTitle, type);
        }
    }

    public static class ConfideRecommendItem extends SystemPushListItem.RemaindAbsPushItem {
        private ConfideMessage confideMessage;
        private StrangerHelper strangerHelper;

        public ConfideRecommendItem(Context context, SimpleProgressHelper simpleProgressHelper,
                                    StrangerHelper strangerHelper, AvatarManager avatarManager,
                                    SystemPush systemPush) {
            super(context, systemPush);
            this.strangerHelper = strangerHelper;
            confideMessage = ConfideMessage.build(systemPush);
        }

        @Override
        protected String getTitle() {
            return confideMessage.getConfide() == null
                    ? null : getString(R.string.role_name,
                    confideMessage.getConfide().getConfideRole());
        }

        @Override
        protected String getSubTitle() {
            return getString(R.string.confide_recommend);
        }

        @Override
        protected void setIcon(CircleImageView circleImageView) {
            if (confideMessage.getConfide() != null) {
                switch (confideMessage.getConfide().getConfideSex()) {
                    case "1":
                        circleImageView.setImageResource(R.drawable.confide_male);
                        break;
                    case "2":
                        circleImageView.setImageResource(R.drawable.confide_female);
                        break;
                    default:
                        circleImageView.setImageResource(R.drawable.confide_male);
                        break;
                }
            }
        }

        @Override
        protected void setImage(ImageView image) {
            image.setVisibility(View.GONE);
        }

        @Override
        protected void setRightTitle(TextView rightTitle) {
            rightTitle.setVisibility(View.GONE);
        }

        @Override
        protected void setConfideContent(TextView textView) {
            if (confideMessage.getConfide() != null) {
                textView.setVisibility(View.VISIBLE);
                textView.setText(confideMessage.getConfide().getConfideContent());
                if (TextUtil.isEmpty(confideMessage.getConfide().getConfideBgImg())) {
                    textView.setBackgroundColor(confideMessage.getConfide().parseBgColor());
                } else {
                    textView.setBackgroundResource(ConfideManager.getInstance(getContext()).getConfideBs().get(confideMessage.getConfide().getConfideBgImg()));
                }
            }
        }

        @Override
        public void onClick() {
            if (confideMessage.getConfide() != null) {
                changetState();
                UILauncher.launchConfideDetaileUI(getContext(), confideMessage.getConfide(), 0);
            }
        }

        @Override
        protected void setVoiceContent(TextView textView) {
            textView.setVisibility(View.GONE);
        }
    }

    public static class InterestItem extends SystemPushListItem.RemaindAbsPushItem {
        private Context mContext;
        private InterestMessage interestMessage;
        private AvatarManager avatarManager;
        private StrangerHelper strangerHelper;


        public InterestItem(Context context, SimpleProgressHelper simpleProgressHelper,
                            StrangerHelper strangerHelper, AvatarManager avatarManager,
                            SystemPush systemPush) {
            super(context, systemPush);
            mContext = context;
            interestMessage = InterestMessage.build(systemPush);
            this.avatarManager = avatarManager;
            this.strangerHelper = strangerHelper;
        }


        @Override
        protected String getTitle() {
            if (interestMessage != null) {
                LiteStranger stranger = interestMessage.getStranger();
                if (stranger != null) {
                    String title = MiscUtils.getUserRemarkName(mContext, stranger.getUserId());
                    return title != null && title.length() > 0 ? title : stranger.getNickname();
                }
            }
            return getString(R.string.unknown);
        }

        @Override
        protected String getSubTitle() {
            return (interestMessage == null || interestMessage.getStranger() == null) ?
                    getString(R.string.unknown)
                    : getString(R.string.interest_remaind_message_name,
                    interestMessage.getStranger().getNickname(),
                    interestMessage.getUserInterest().getinterestTypeName());
        }

        @Override
        protected void setIcon(CircleImageView circleImageView) {
            if (interestMessage != null && interestMessage.getStranger() != null) {
                MiscUtils.showAvatarThumb(avatarManager, interestMessage.getStranger().getAvatarThumb(),
                        circleImageView, R.drawable.contact_single);
                circleImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        strangerHelper.showStranger(interestMessage.getStranger().getUserId());
                    }
                });
            }
        }

        @Override
        protected void setImage(ImageView image) {
            image.setVisibility(View.GONE);
        }

        @Override
        protected void setRightTitle(TextView rightTitle) {
            if (interestMessage != null && interestMessage.getUserInterest() != null) {
                rightTitle.setVisibility(View.VISIBLE);
                UserInterest interest = interestMessage.getUserInterest();
                rightTitle.setText(interest.getInterestName());
                InterestUtils.setInterestColor(getContext(), rightTitle, interest.getInterestType());
            }
        }

        @Override
        protected void setConfideContent(TextView textView) {
            textView.setVisibility(View.GONE);
        }

        @Override
        public void onClick() {
            changetState();
            if (interestMessage != null && interestMessage.getStranger() != null) {
                strangerHelper.showStranger(interestMessage.getStranger().getUserId());
            }
        }

        @Override
        protected void setVoiceContent(TextView textView) {
            textView.setVisibility(View.GONE);
        }
    }

    public static class DynamicItem extends SystemPushListItem.RemaindAbsPushItem {

        private Context context;
        private final DynamicRemaindMessage message;
        private AvatarManager avatarManager;
        private StrangerHelper strangerHelper;
        private SimpleProgressHelper simpleProgressHelper;


        public DynamicItem(Context context, SimpleProgressHelper simpleProgressHelper,
                           StrangerHelper strangerHelper, AvatarManager avatarManager,
                           SystemPush systemPush) {
            super(context, systemPush);
            this.context = context;
            this.simpleProgressHelper = simpleProgressHelper;
            this.avatarManager = avatarManager;
            this.strangerHelper = strangerHelper;
            this.message = DynamicRemaindMessage.build(systemPush);

            if (message == null) {
                throw new IllegalArgumentException("Illegal SystemPush content");
            }
        }

        @Override
        protected String getTitle() {
            LiteStranger stranger = message.getStranger();
            if (stranger != null) {
                String title = MiscUtils.getUserRemarkName(context, stranger.getUserId());
                return title != null && title.length() > 0 ? title : stranger.getNickname();
            } else {
                return getString(R.string.unknown);
            }
        }

        @Override
        protected String getSubTitle() {
            String subTitle = getString(R.string.unknown);
            LabelStory story = message.getLabelStory();
            LiteStranger stranger = message.getStranger();

            if (story != null && stranger != null) {
                switch (story.getType()) {
                    case LabelStory.TYPE_ONLINEAUDIO:
                    case LabelStory.TYPE_AUDIO:
                        subTitle = getString(R.string.voice_remaind_message_name,
                                stranger.getNickname());
                        break;
                    case LabelStory.TYPE_TXT_IMG:
                        subTitle = getString(R.string.image_remaind_message_name,
                                stranger.getNickname());
                        break;
                    case LabelStory.TYPE_BANKNOTE:
                        subTitle = getString(R.string.banknote_remaind_message_name,
                                stranger.getNickname());
                        break;
                    default:
                        break;
                }
            }
            return subTitle;
        }

        @Override
        protected void setIcon(CircleImageView circleImageView) {
            if (message != null && message.getStranger() != null) {
                MiscUtils.showAvatarThumb(avatarManager, message.getStranger().getAvatarThumb(),
                        circleImageView, R.drawable.contact_single);
                circleImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        strangerHelper.showStranger(message.getStranger().getUserId());
                    }
                });
            }
        }

        @Override
        protected void setImage(ImageView image) {
            if (message != null && message.getLabelStory() != null) {
                LabelStory labelStory = message.getLabelStory();
                switch (labelStory.getType()) {
                    case LabelStory.TYPE_AUDIO:
                        image.setVisibility(View.VISIBLE);
                        image.setImageResource(R.drawable.sound_play_bg);
                        break;
                    case LabelStory.TYPE_TXT_IMG:
                        if (labelStory.getThumbImages() != null) {
                            image.setVisibility(View.VISIBLE);
                            MiscUtils.showLabelStoryCommentAvatarThumb(avatarManager, labelStory.getThumbImages()[0], image, R.drawable.image_load_fail);
                        } else {
                            image.setVisibility(View.GONE);
                        }
                        break;
                    case LabelStory.TYPE_BANKNOTE:
                        if (labelStory.getThumbImages() != null) {
                            image.setVisibility(View.VISIBLE);
                            MiscUtils.showLabelStoryCommentAvatarThumb(avatarManager, labelStory.getThumbImages()[0], image, R.drawable.image_load_fail);
                        } else {
                            image.setVisibility(View.GONE);
                        }
                        break;
                    case LabelStory.TYPE_ONLINEAUDIO:
                        image.setVisibility(View.VISIBLE);
                        if (labelStory.getThumbImages() != null) {
                            avatarManager.displaySingerAvatar(labelStory.getThumbImages()[0], image, R.drawable.sound_play_bg);
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
        protected void setRightTitle(TextView rightTitle) {
            rightTitle.setVisibility(View.GONE);
        }

        @Override
        protected void setConfideContent(TextView textView) {
            textView.setVisibility(View.GONE);
        }

        @Override
        protected void setVoiceContent(TextView textView) {
            if (message != null && message.getLabelStory() != null) {
                if (LabelStory.TYPE_AUDIO.equals(message.getLabelStory().getType())) {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(message.getLabelStory().getContent());
                } else if (LabelStory.TYPE_ONLINEAUDIO.equals(message.getLabelStory().getType())){
                    textView.setVisibility(View.VISIBLE);
                    if (!TextUtil.isEmpty(message.getLabelStory().getContent())) {
                        try {
                            JSONObject json = new JSONObject(message.getLabelStory().getContent());
                            textView.setText(json.getString(CommandFields.Dynamic.DYNAMIC_CONTENT));
                        } catch (Exception e) {
                            textView.setVisibility(View.GONE);
                        }
                    } else {
                        textView.setVisibility(View.GONE);
                    }
                }else{
                    textView.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onClick() {
            changetState();
            if (message != null && message.getLabelStory() != null) {
                DynamicArguments arguments = new DynamicArguments();
                arguments.setLabelStory(message.getLabelStory());
                arguments.setIsShowTitle(true);
                arguments.setIsShowFragment(true);
                arguments.setIsComment(false);
                DynamicDetailsHelper dynamicDetailsHelper = new DynamicDetailsHelper(
                        getContext(), simpleProgressHelper, arguments);
                dynamicDetailsHelper.loadDynamicDetails();
            }
        }
    }

    public static class UserTagItem extends SystemPushListItem.RemaindAbsPushItem {
        private Context mContext;
        private UserTagMessage userTagMessage;
        private AvatarManager avatarManager;
        private StrangerHelper strangerHelper;


        public UserTagItem(Context context, SimpleProgressHelper simpleProgressHelper,
                           StrangerHelper strangerHelper, AvatarManager avatarManager,
                           SystemPush systemPush) {
            super(context, systemPush);
            mContext = context;
            userTagMessage = UserTagMessage.build(systemPush);
            this.avatarManager = avatarManager;
            this.strangerHelper = strangerHelper;

        }


        @Override
        protected String getTitle() {
            if (userTagMessage != null) {
                LiteStranger stranger = userTagMessage.getStranger();
                if (stranger != null) {
                    String title = MiscUtils.getUserRemarkName(mContext, stranger.getUserId());
                    return title != null && title.length() > 0 ? title : stranger.getNickname();
                }
            }
            return getString(R.string.unknown);
        }

        @Override
        protected String getSubTitle() {
            return (userTagMessage == null || userTagMessage.getStranger() == null) ?
                    getString(R.string.unknown) : getString(R.string.usertag_remaind_message_name, userTagMessage.getStranger().getNickname());
        }

        @Override
        protected void setIcon(CircleImageView circleImageView) {
            if (userTagMessage != null && userTagMessage.getStranger() != null) {
                MiscUtils.showAvatarThumb(avatarManager, userTagMessage.getStranger().getAvatarThumb(),
                        circleImageView, R.drawable.contact_single);
                circleImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        strangerHelper.showStranger(userTagMessage.getStranger().getUserId());
                    }
                });
            }
        }

        @Override
        protected void setImage(ImageView image) {
            image.setVisibility(View.GONE);
        }

        @Override
        protected void setRightTitle(TextView rightTitle) {
            if (userTagMessage != null && userTagMessage.getUserTag() != null) {
                rightTitle.setVisibility(View.VISIBLE);
                rightTitle.setText(userTagMessage.getUserTag().getTagName());
                rightTitle.setTextColor(Color.WHITE);
                GradientDrawable drawable = (GradientDrawable) getContext().getResources()
                        .getDrawable(R.drawable.corners_bg);
                UserTag tag = userTagMessage != null ? userTagMessage.getUserTag() : null;
                if (tag != null && drawable != null) {
                    drawable.setColor(tag.parseTagSelectedColor());
                    CompatUtils.setBackground(rightTitle, drawable);
                }
            }
        }

        @Override
        protected void setConfideContent(TextView textView) {
            textView.setVisibility(View.GONE);
        }

        @Override
        public void onClick() {
            changetState();
            if (userTagMessage != null && userTagMessage.getStranger() != null) {
                strangerHelper.showStranger(userTagMessage.getStranger().getUserId());
            }
        }

        @Override
        protected void setVoiceContent(TextView textView) {
            textView.setVisibility(View.GONE);
        }
    }
}

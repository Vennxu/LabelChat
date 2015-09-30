package com.ekuater.labelchat.ui.fragment.push;

import android.support.annotation.Nullable;

import com.ekuater.labelchat.datastruct.BeenFollowedMessage;
import com.ekuater.labelchat.datastruct.BeenInvitedMessage;
import com.ekuater.labelchat.datastruct.ConfideMessage;
import com.ekuater.labelchat.datastruct.DynamicOperateMessage;
import com.ekuater.labelchat.datastruct.DynamicRemaindMessage;
import com.ekuater.labelchat.datastruct.FollowUser;
import com.ekuater.labelchat.datastruct.InteractMessage;
import com.ekuater.labelchat.datastruct.InterestMessage;
import com.ekuater.labelchat.datastruct.LiteStranger;
import com.ekuater.labelchat.datastruct.PhotoNotifyMessage;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.SystemPushType;
import com.ekuater.labelchat.datastruct.UserTagMessage;
import com.ekuater.labelchat.util.TextUtil;


/**
 * Created by Administrator on 2015/4/30.
 */
public class SystemPushUtils {

    public static final String SYSTEM_PUSH_TYPE = "system_push_type";
    public static final int SYSTEM_PUSH_RED = 0;
    public static final int SYSTEM_PUSH_COMMENT = 1;
    public static final int SYSTEM_PUSH_PRAISE = 2;
    public static final int SYSTEM_PUSH_REMIND = 3;
    public static final int SYSTEM_PUSH_OTHER = 4;

    public static int[] types = new int[]{SystemPushType.TYPE_LABEL_STORY_COMMENTS, SystemPushType.TYPE_BEEN_FOLLOWED, SystemPushType.TYPE_PHOTO_NOTIFY,
            SystemPushType.TYPE_BEEN_INVITED, SystemPushType.TYPE_CONFIDE_COMMEND, SystemPushType.TYPE_TAG_INTERACT, SystemPushType.TYPE_UPLOAD_PHOTO,
            SystemPushType.TYPE_CONFIDE_RECOMMEND, SystemPushType.TYPE_REMAIND_INTEREST, SystemPushType.TYPE_REMAIND_DYNAMIC, SystemPushType.TYPE_REMAIND_TAG};

    public static int[] getFliterType(int type) {
        int[] showType = null;
        switch (type) {
            case SYSTEM_PUSH_COMMENT:
                showType = SystemPushType.COMMENT;
                break;
            case SYSTEM_PUSH_PRAISE:
                showType = SystemPushType.PRAISE;
                break;
            case SYSTEM_PUSH_REMIND:
                showType = SystemPushType.REMIND;
                break;
        }
        return showType;
    }


    public static int getNowType(SystemPush systemPush) {
        int nowType = SYSTEM_PUSH_RED;
        switch (systemPush.getType()) {
            case SystemPushType.TYPE_LABEL_STORY_COMMENTS:
                DynamicOperateMessage dynamic = DynamicOperateMessage.build(systemPush);
                if (dynamic != null) {
                    if (DynamicOperateMessage.TYPE_MESSAGE_BOX.equals(dynamic.getMessagePlace())) {
                        nowType = DynamicOperateMessage.TYPE_OPERATE_COMMENT.equals(dynamic.getOperateType()) ? SYSTEM_PUSH_COMMENT : SYSTEM_PUSH_PRAISE;
                    }
                }
                break;
            case SystemPushType.TYPE_BEEN_FOLLOWED:
                nowType = SYSTEM_PUSH_REMIND;
                break;
            case SystemPushType.TYPE_PHOTO_NOTIFY:
                PhotoNotifyMessage photo = PhotoNotifyMessage.build(systemPush);
                if (photo != null)
                    nowType = PhotoNotifyMessage.TYPE_HAS_PRAISE.equals(photo.getNotifyType()) ? SYSTEM_PUSH_PRAISE : SYSTEM_PUSH_REMIND;
                break;
            case SystemPushType.TYPE_BEEN_INVITED:
                nowType = SYSTEM_PUSH_REMIND;
                break;
            case SystemPushType.TYPE_CONFIDE_COMMEND:
                ConfideMessage confide = ConfideMessage.build(systemPush);
                if (confide != null)
                    if (DynamicOperateMessage.TYPE_MESSAGE_BOX.equals(confide.getMessagePlace()) || TextUtil.isEmpty(confide.getMessagePlace())) {
                        nowType = DynamicOperateMessage.TYPE_OPERATE_COMMENT.equals(confide.getOperateType()) ? SYSTEM_PUSH_COMMENT : SYSTEM_PUSH_PRAISE;
                    }
                break;
            case SystemPushType.TYPE_TAG_INTERACT:
            case SystemPushType.TYPE_UPLOAD_PHOTO:
            case SystemPushType.TYPE_CONFIDE_RECOMMEND:
            case SystemPushType.TYPE_REMAIND_DYNAMIC:
            case SystemPushType.TYPE_REMAIND_TAG:
            case SystemPushType.TYPE_REMAIND_INTEREST:
                nowType = SYSTEM_PUSH_REMIND;
                break;
            default:
                break;
        }
        return nowType;
    }

    public static CommentPush getCommentJsonContent(SystemPush systemPush) {
        CommentPush push = null;
        switch (systemPush.getType()) {
            case SystemPushType.TYPE_LABEL_STORY_COMMENTS:
                DynamicOperateMessage dynamic = DynamicOperateMessage.build(systemPush);
                push = new CommentPush();
                push.setGroupType(SYSTEM_PUSH_COMMENT);
                push.setChildType(CommentPush.DYNAMIC);
                push.setReply(dynamic.getReplyDynamicCommentContent());
                push.setStranger(dynamic.getStranger());
                push.setFlag(dynamic.getDynamicType());
                break;
            case SystemPushType.TYPE_CONFIDE_COMMEND:
                ConfideMessage confide = ConfideMessage.build(systemPush);
                push = new CommentPush();
                push.setGroupType(SYSTEM_PUSH_COMMENT);
                push.setChildType(CommentPush.CONFIDE);
                push.setReply(confide.getReplyCommentContent());
                push.setStranger(confide.getStranger());
                break;
        }
        return push;
    }

    public static PraisePush getPraiseJsonContent(SystemPush systemPush) {
        PraisePush push = null;
        switch (systemPush.getType()) {
            case SystemPushType.TYPE_LABEL_STORY_COMMENTS:
                DynamicOperateMessage dynamic = DynamicOperateMessage.build(systemPush);
                push = new PraisePush();
                push.setGroupType(SYSTEM_PUSH_PRAISE);
                push.setChildType(PraisePush.DYNAMIC);
                push.setStranger(dynamic.getStranger());
                push.setFlag(dynamic.getDynamicType());
                break;
            case SystemPushType.TYPE_CONFIDE_COMMEND:
                ConfideMessage confide = ConfideMessage.build(systemPush);
                push = new PraisePush();
                push.setGroupType(SYSTEM_PUSH_PRAISE);
                push.setChildType(PraisePush.CONFIDE);
                push.setStranger(confide.getStranger());
                break;
            case SystemPushType.TYPE_PHOTO_NOTIFY:
                PhotoNotifyMessage photo = PhotoNotifyMessage.build(systemPush);
                push = new PraisePush();
                push.setGroupType(SYSTEM_PUSH_PRAISE);
                push.setChildType(PraisePush.PHOTO);
                LiteStranger liteStranger = photo.getNotifyUser();
                if (liteStranger != null) {
                    push.setStranger(new Stranger(liteStranger.getUserId(), liteStranger.getNickname(), liteStranger.getAvatarThumb(), null, 0));
                }
                break;
        }
        return push;
    }

    @Nullable
    public static RemaindPush getRemaindJsonContent(SystemPush systemPush) {
        RemaindPush push = null;

        switch (systemPush.getType()) {
            case SystemPushType.TYPE_BEEN_FOLLOWED:
                BeenFollowedMessage follow = BeenFollowedMessage.build(systemPush);
                push = new RemaindPush();
                push.setGroupType(SYSTEM_PUSH_REMIND);
                push.setRemindType(RemaindPush.FOLLOW);
                push.setRemindFlag(follow.getFollowType());
                FollowUser user = follow.getFollowUser();
                if (user != null) {
                    push.setStranger(new Stranger(user.getUserId(), user.getNickname(), user.getAvatarThumb(), user.getAvatar(), 0));
                }
                break;
            case SystemPushType.TYPE_PHOTO_NOTIFY:
                PhotoNotifyMessage photo = PhotoNotifyMessage.build(systemPush);
                push = new RemaindPush();
                push.setGroupType(SYSTEM_PUSH_REMIND);
                push.setRemindType(RemaindPush.PHOTO);
                push.setRemindFlag(Integer.parseInt(photo.getNotifyType()));
                LiteStranger liteStranger = photo.getNotifyUser();
                if (liteStranger != null) {
                    push.setStranger(new Stranger(liteStranger.getUserId(), liteStranger.getNickname(), liteStranger.getAvatarThumb(), null, 0));
                }
                break;
            case SystemPushType.TYPE_BEEN_INVITED:
                BeenInvitedMessage invitedMessage = BeenInvitedMessage.build(systemPush);
                push = new RemaindPush();
                push.setGroupType(SYSTEM_PUSH_REMIND);
                push.setRemindType(RemaindPush.INVITED);
                LiteStranger invitedStranger = invitedMessage.getStranger();
                if (invitedStranger != null) {
                    push.setStranger(new Stranger(invitedStranger.getUserId(), invitedStranger.getNickname(), invitedStranger.getAvatarThumb(), null, 0));
                }
                break;
            case SystemPushType.TYPE_TAG_INTERACT:
                InteractMessage interactMessage = InteractMessage.build(systemPush);
                push = new RemaindPush();
                push.setGroupType(SYSTEM_PUSH_REMIND);
                push.setRemindType(RemaindPush.INTERACT);
                if (interactMessage.getInteract() != null) {
                    push.setRemindFlag(Integer.parseInt(interactMessage.getInteract().getInteractType()));
                    LiteStranger stranger = interactMessage.getInteract().getStranger();
                    if (stranger != null) {
                        push.setStranger(new Stranger(stranger.getUserId(), stranger.getNickname(), stranger.getAvatarThumb(), null, 0));
                    }
                }
                break;
            case SystemPushType.TYPE_UPLOAD_PHOTO:
                PhotoNotifyMessage photoNotifyMessage = PhotoNotifyMessage.build(systemPush);
                push = new RemaindPush();
                push.setGroupType(SYSTEM_PUSH_REMIND);
                push.setRemindType(RemaindPush.NEW_PHOTO);
                if (photoNotifyMessage != null) {
                    LiteStranger uploadPhotoUserInfo = photoNotifyMessage.getNotifyUser();
                    if (uploadPhotoUserInfo != null) {
                        push.setStranger(new Stranger(uploadPhotoUserInfo.getUserId(), uploadPhotoUserInfo.getNickname(),
                                uploadPhotoUserInfo.getAvatarThumb(), null, 0));
                    }
                }
                break;
            case SystemPushType.TYPE_CONFIDE_RECOMMEND:
                push = new RemaindPush();
                push.setGroupType(SYSTEM_PUSH_REMIND);
                push.setRemindType(RemaindPush.CONFIDE_RECOMMEND);
                break;
            case SystemPushType.TYPE_REMAIND_INTEREST:
                InterestMessage interestMessage = InterestMessage.build(systemPush);
                push = new RemaindPush();
                push.setGroupType(SYSTEM_PUSH_REMIND);
                push.setRemindType(RemaindPush.INTEREST);
                if (interestMessage != null) {
                    if (interestMessage.getUserInterest() != null) {
                        push.setFlag(interestMessage.getUserInterest().getinterestTypeName());
                    }
                    LiteStranger stranger = interestMessage.getStranger();
                    if (stranger != null) {
                        push.setStranger(new Stranger(stranger.getUserId(), stranger.getNickname(),
                                stranger.getAvatarThumb(), null, 0));
                    }
                }
                break;
            case SystemPushType.TYPE_REMAIND_DYNAMIC:
                DynamicRemaindMessage message = DynamicRemaindMessage.build(systemPush);
                push = new RemaindPush();
                push.setGroupType(SYSTEM_PUSH_REMIND);
                push.setRemindType(RemaindPush.DYNAMIC);
                if (message != null) {
                    push.setFlag(message.getLabelStory().getType());
                    LiteStranger stranger = message.getStranger();
                    if (stranger != null) {
                        push.setStranger(new Stranger(stranger.getUserId(), stranger.getNickname(),
                                stranger.getAvatarThumb(), null, 0));
                    }
                }
                break;
            case SystemPushType.TYPE_REMAIND_TAG:
                UserTagMessage userTagMessage = UserTagMessage.build(systemPush);
                push = new RemaindPush();
                push.setGroupType(SYSTEM_PUSH_REMIND);
                push.setRemindType(RemaindPush.USERTAG);
                if (userTagMessage != null) {
                    LiteStranger stranger = userTagMessage.getStranger();
                    if (stranger != null) {
                        push.setStranger(new Stranger(stranger.getUserId(), stranger.getNickname(),
                                stranger.getAvatarThumb(), null, 0));
                    }
                }
                break;
            default:
                break;
        }
        return push;
    }

    public static boolean getFliterType(int target, SystemPush push) {

        boolean isAccept = false;
        switch (target) {
            case SYSTEM_PUSH_COMMENT:
                if (SystemPushType.TYPE_LABEL_STORY_COMMENTS == push.getType()) {
                    DynamicOperateMessage dynamic = DynamicOperateMessage.build(push);
                    if (dynamic != null) {
                        if (DynamicOperateMessage.TYPE_MESSAGE_BOX.equals(dynamic.getMessagePlace())) {
                            isAccept = DynamicOperateMessage.TYPE_OPERATE_COMMENT.equals(dynamic.getOperateType());
                        }
                    }
                } else if (SystemPushType.TYPE_CONFIDE_COMMEND == push.getType()) {
                    ConfideMessage confide = ConfideMessage.build(push);
                    if (confide != null) {
                        if (DynamicOperateMessage.TYPE_MESSAGE_BOX.equals(confide.getMessagePlace())) {
                            isAccept = ConfideMessage.TYPE_OPERATE_COMMENT.equals(confide.getOperateType());
                        }
                    }
                }
                break;
            case SYSTEM_PUSH_PRAISE:
                if (SystemPushType.TYPE_LABEL_STORY_COMMENTS == push.getType()) {
                    DynamicOperateMessage dynamic = DynamicOperateMessage.build(push);
                    if (dynamic != null) {
                        if (DynamicOperateMessage.TYPE_MESSAGE_BOX.equals(dynamic.getMessagePlace())) {
                            isAccept = DynamicOperateMessage.TYPE_OPERATE_PRAISE.equals(dynamic.getOperateType());
                        }
                    }
                } else if (SystemPushType.TYPE_CONFIDE_COMMEND == push.getType()) {
                    ConfideMessage confide = ConfideMessage.build(push);
                    if (confide != null) {
                        isAccept = ConfideMessage.TYPE_OPERATE_PRAISE.equals(confide.getOperateType());
                    }
                } else if (SystemPushType.TYPE_PHOTO_NOTIFY == push.getType()) {
                    PhotoNotifyMessage photo = PhotoNotifyMessage.build(push);
                    isAccept = PhotoNotifyMessage.TYPE_HAS_PRAISE.equals(photo.getNotifyType());
                }
                break;
            case SYSTEM_PUSH_REMIND:
                if (SystemPushType.TYPE_PHOTO_NOTIFY == push.getType()) {
                    PhotoNotifyMessage photo = PhotoNotifyMessage.build(push);
                    if (photo != null) {
                        isAccept = !PhotoNotifyMessage.TYPE_HAS_PRAISE.equals(photo.getNotifyType());
                    }
                } else {
                    isAccept = true;
                }
                break;
            case SYSTEM_PUSH_OTHER:
                for (int i = 0; i < types.length; i++) {
                    if (types[i] == push.getType()) {
                        isAccept = false;
                        break;
                    } else {
                        isAccept = true;
                    }
                }
                break;
        }
        return isAccept;
    }

}

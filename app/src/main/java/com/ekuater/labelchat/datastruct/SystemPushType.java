package com.ekuater.labelchat.datastruct;

/**
 * SystemPush message type definition
 *
 * @author LinYong
 */
public class SystemPushType {

    public static final int TYPE_ILLEGAL = -1; // illegal type

    public static final int TYPE_VALIDATE_ADD_FRIEND = 1;
    public static final int TYPE_ADD_FRIEND = 2;
    public static final int TYPE_FRIEND_INFO_UPDATED = 3;
    public static final int TYPE_ADD_FRIEND_AGREE_RESULT = 4;
    public static final int TYPE_ADD_FRIEND_REJECT_RESULT = 5;
    public static final int TYPE_DEFRIEND_NOTIFICATION = 6;

    public static final int TYPE_CONFIRM_WEEKLY_STAR = 100;
    public static final int TYPE_WEEKLY_STAR = 101;
    public static final int TYPE_BUBBLE_UP = 102;
    public static final int TYPE_TODAY_RECOMMENDED = 103;
    public static final int TYPE_SYSTEM_RECOMMEND_FRIEND = 104;

    public static final int TYPE_WEEKLY_HOT_LABEL = 201;
    public static final int TYPE_RECOMMEND_LABEL = 202;
    public static final int TYPE_STRANGER_RECOMMEND_LABEL = 203;

    public static final int TYPE_LOGIN_ON_OTHER_CLIENT = 301;
    public static final int TYPE_REGISTER_WELCOME = 302;

    public static final int TYPE_TMP_GROUP_CREATE = 401;
    public static final int TYPE_TMP_GROUP_DISMISS = 402;
    public static final int TYPE_TMP_GROUP_MEMBER_QUIT = 403;
    public static final int TYPE_TMP_GROUP_DISMISS_REMIND = 404;

    public static final int TYPE_LABEL_STORY_COMMENTS = 501;
    public static final int TYPE_LABEL_STORY_TIP = 502;
    public static final int TYPE_PRIVATE_LETTER = 503;
    public static final int TYPE_BEEN_FOLLOWED = 504;
    public static final int TYPE_PHOTO_NOTIFY = 505;
    public static final int TYPE_BEEN_INVITED = 506;
    public static final int TYPE_CONFIDE_COMMEND = 507;
    public static final int TYPE_TAG_INTERACT = 508;
    public static final int TYPE_CONFIDE_RECOMMEND=509;
    public static final int TYPE_UPLOAD_PHOTO = 510;
    public static final int TYPE_REMAIND_TAG = 511;
    public static final int TYPE_REMAIND_INTEREST = 512;
    public static final int TYPE_REMAIND_DYNAMIC = 513;

    // Local define push message for UI
    public static final int TYPE_LOCAL_TMP_GROUP_DISMISSED = 1001;
    public static final int TYPE_LOCAL_TMP_GROUP_EXPIRED = 1002;
    public static final int TYPE_LOCAL_PHOTO_NOTIFY_SAW = 1011;
    public static final int TYPE_LOCAL_PHOTO_NOTIFY_REMINDED = 1012;
    public static final int TYPE_LOCAL_PHOTO_NOTIFY_PRAISE=1013;

    public static final int TYPE_LOCAL_DYNAMIC_PRAISE_NOTIFY = 1021;
    public static final int TYPE_LOCAL_DYNAMIC_COMMENTS_NOTIFY = 1022;

    public static final int TYPE_LOCAL_CONFIDE_PRAISE_NOTIFY = 1031;
    public static final int TYPE_LOCAL_CONFIDE_COMMENT_NOTIFY = 1032;

    public static int[] COMMENT = new int[]{TYPE_LABEL_STORY_COMMENTS, TYPE_CONFIDE_COMMEND};
    public static int[] PRAISE = new int[]{TYPE_LABEL_STORY_COMMENTS, TYPE_CONFIDE_COMMEND, TYPE_PHOTO_NOTIFY};
    public static int[] REMIND = new int[]{TYPE_BEEN_FOLLOWED, TYPE_PHOTO_NOTIFY, TYPE_BEEN_INVITED, TYPE_TAG_INTERACT,
            TYPE_UPLOAD_PHOTO,TYPE_CONFIDE_RECOMMEND,TYPE_REMAIND_TAG, TYPE_REMAIND_INTEREST, TYPE_REMAIND_DYNAMIC, TYPE_REMAIND_TAG};
}

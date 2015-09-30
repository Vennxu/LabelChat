package com.ekuater.labelchat.command;

/**
 * @author LinYong
 */
public final class CommandUrl {

    // account manager url
    public static final String ACCOUNT_MOBILE_VERIFY_CODE = "/api/user/mobileVerifyCode";
    public static final String ACCOUNT_CHECK_VERIFY_CODE = "/api/user/confirmVerifyCode";
    public static final String ACCOUNT_REGISTER = "/api/user/register";
    public static final String ACCOUNT_LOGIN = "/api/user/login";
    public static final String ACCOUNT_UPDATE_INFO = "/api/user/updateInfo";
    public static final String ACCOUNT_LOGOUT = "/api/user/logout";
    public static final String ACCOUNT_UPDATE_LOCATION = "/api/user/updatePosition";
    public static final String ACCOUNT_AUTHORIZE = "/api/user/authorize";
    public static final String ACCOUNT_MODIFY_PASSWORD = "/api/user/modifyPass";
    public static final String ACCOUNT_RESET_PASSWORD = "/api/user/resetPassword";
    public static final String ACCOUNT_QUERY_PERSONAL_INFO = "/api/user/findUser";
    public static final String ACCOUNT_UPLOAD_AVATAR = "/api/user/uploadAvatar";
    public static final String ACCOUNT_PULL_SYSTEM_PUSH = "/api/message/pullMessage";
    // Third platform OAUTH login
    public static final String ACCOUNT_OAUTH_LOGIN = "/api/thirduser/login";
    public static final String ACCOUNT_OAUTH_BIND_ACCOUNT = "/api/thirduser/boundMobile";
    // RongCloud
    public static final String ACCOUNT_RONGCLOUD_GET_TOKEN = "/api/rcloud/getToken";

    // label command url
    public static final String LABEL_ADD_USER_LABEL = "/api/label/addLabel";
    public static final String LABEL_DEL_USER_LABEL = "/api/label/deleteLabel";
    public static final String LABEL_ENUM_USER_LABEL = "/api/label/userLabel";
    public static final String LABEL_QUERY_SYS_LABEL = "/api/label/searchLabel";
    public static final String LABEL_LIST_SYS_LABEL = "/api/label/showSysLabel";
    public static final String LABEL_PRAISE_COUNT = "/api/labelStory/userLabelPraise";
    public static final String LABEL_RECOMMEND_LABEL = "/api/label/recommendLabel";
    public static final String LABEL_RECOMMEND_STRANGER_LABEL = "/api/label/recommendStrangerLabel";
    public static final String LABEL_HOT_LABEL = "/api/hotLabel/list";
    public static final String LABEL_RANKING_SYS_LABEL = "/api/label/labelRanking";

    public static final String SETTING_PRIVACY = "/api/lbPrivacy/updateLbPrivacyInfo";
    public static final String SETTING_LABEL = "/api/lbSetting/updateLbSettingInfo";
    public static final String SETTING_CHAT = "/api/lbChat/updateLbChatInfo";
    public static final String SETTING_USER = "/api/user/updateInfo";

    // contact manager about url
    public static final String CONTACT_ADD_FRIEND_REQUEST = "/api/FriendRequest/addFriendRequest";
    public static final String CONTACT_LABEL_QUERY_FRIEND = "/api/friend/searchFriendBylabel";
    public static final String CONTACT_QUERY_NEARBY_USER = "/api/friend/queryNearbyUser";
    public static final String CONTACT_BUBBLING = "/api/bubbling/create";
    public static final String CONTACT_QUERY_USER_INFO = "/api/friend/strangerInfo";
    public static final String CONTACT_TODAY_RECOMMENDED = "/api/todaycommend/create";
    public static final String CONTACT_SYNC = "/api/friend/myFriends";
    public static final String CONTACT_WEEKLY_STAR_CONFIRM = ""; // TODO
    public static final String CONTACT_DELETE_FRIEND = "/api/friend/deleteFriend";
    public static final String CONTACT_MODIFY_FRIEND_REMARK = "/api/friend/modifyFriendRemark";
    public static final String CONTACT_VALID_FRIEND_REQUEST = "/api/friend/validFriendRequest";
    public static final String CONTACT_EXACT_SEARCH = "/api/friend/exactSearch";
    public static final String CONTACT_RAND_USERS = "/api/user/randUsers";
    public static final String CONTACT_QUERY_FRIEND_INFO = "/api/friend/friendInfo";
    public static final String CONTACT_QUERY_FRIEND_BY_ONE_LABEL = "/api/friend/searchFriendByOnelabel";
    public static final String CONTACT_QUERY_LITE_USER = "/api/friend/strangerSimpleInfo";

    public static final String MISC_FEEDBACK = "/api/feedback/create";
    public static final String MISC_COMPLAIN = "/api/complain/send";
    public static final String NEW_USER = "/api/user/newUsers";

    // tmp group
    public static final String TMP_GROUP_CREATE = "/api/tempGroup/createGroup";
    public static final String TMP_GROUP_DISMISS = "/api/tempGroup/dismissGroup";
    public static final String TMP_GROUP_GROUP_INFO = "/api/tempGroup/groupInfo";
    public static final String TMP_GROUP_MEMBER_QUIT = "/api/tempGroup/quitGroup";
    public static final String TMP_GROUP_GROUP_TIME = "/api/tempGroup/groupTime";

    //story label
    public static final String LABEL_STORY_LIST = "/api/labelStory/categoryList";
    public static final String LABEL_STORY_POST = "/api/labelStory/post";
    public static final String LABEL_STORY_IMAGEPOST = "/api/labelStory/postTextAndImg";
    public static final String LABEL_STORY_PRAISE = "/api/labelStory/praise";
    public static final String LABEL_STORY_COMMAND = "/api/labelStory/comment";
    public static final String LABEL_STORY_COMMANDLIST = "/api/labelStory/comment/list";
    public static final String LABEL_STORY_LATESTSTORY = "/api/labelStory/latestStory";
    public static final String LABEL_STORY_MY_STORY = "/api/labelStory/myStory";
    public static final String LABEL_STORY_COMMENTS_PRAISE = "/api/labelStory/commentPraise";
    public static final String LABEL_STORY_REPLY_COMMENTS = "/api/labelStory/reComment";
    public static final String LABEL_STORY_SHARE_STATISTICS = "/api/labelStory/share";
    public static final String LABEL_STORY_GRADE = "/api/labelStory/grade";
    public static final String LABEL_STORY_DELETE = "/api/labelStory/deleteStory";
    public static final String LABEL_STORY_CATEGORY = "/api/dynamicCategory/list";
    public static final String LABEL_STORY_LETTER_SEND = "/api/message/sendSecretMsg";
    public static final String LABEL_STORY_LATELY_ONE_STORY = "/api/labelStory/latestOneStory";
    public static final String LABEL_STORY_FOLLOW_LIST = "api/labelStory/followingStory";
    public static final String LABEL_STORY_POST_MEDIA = "/api/labelStory/postMedia";

    public static final String SEND_INVITE_NOTIFY = "/api/message/sendInviteNotify";

    public static final String MIX_DYNAMIC_GLOBAL_LIST = "/api/dynamic/globalList";
    public static final String MIX_DYNAMIC_RELATED_LIST = "/api/dynamic/relatedList";
    public static final String MIX_DYNAMIC_MY_DYNAMIC = "/api/dynamic/myDynamic";

    // Throw photo
    public static final String THROW_PHOTO_THROW = "/api/throwPhoto/create";
    public static final String THROW_PHOTO_MY_THROW = "/api/throwPhoto/myThrowPhoto";
    public static final String THROW_PHOTO_MAP_SHOW = "/api/throwPhoto/mapShow";
    public static final String THROW_PHOTO_PICK = "/api/throwPhoto/pickPhoto";
    public static final String THROW_PHOTO_DELETE = "/api/throwPhoto/deleteThrowPhoto";

    public static final String THEME_LIST = "/api/theme/list";

    public static final String CHAT_ROOM_LIST = "/api/chatRoom/list";
    public static final String CHAT_ROOM_JOIN = "/api/chatRoom/join";
    public static final String CHAT_ROOM_QUIT = "/api/chatRoom/out";
    public static final String CHAT_ROOM_MEMBERS = "/api/chatRoom/members";
    public static final String CHAT_ROOM_MEMBER_COUNT = "/api/chatRoom/onlineCount";

    //FOLLOWING
    public static final String FOLLOWING = "/api/follow/following";
    public static final String UN_FOLLOWING = "/api/follow/unFollowing";
    public static final String IS_FOLLOWING = "/api/follow/isFollowing";
    public static final String COUNT_FOLLOWING = "/api/follow/followingCount";
    public static final String COUNT_FOLLOWER = "/api/follow/followerCount";
    public static final String LIST_FOLLOWING = "/api/follow/followingList";
    public static final String LIST_FOLLOWER = "/api/follow/followerList";

    // User tag
    public static final String TAG_LIST_TAG_TYPE = "/api/tag/tagList";
    public static final String TAG_GET_TAG = "/api/tag/userTag";
    public static final String TAG_SET_TAG = "/api/tag/userSettingTag";

    // Album
    public static final String ALBUM_UPLOAD_PHOTO = "/api/photo/upload";
    public static final String ALBUM_LIST_PHOTOS = "/api/photo/myPhoto";
    public static final String ALBUM_LATEST_PHOTOS = "/api/photo/userLatestPhoto";
    public static final String ALBUM_DELETE_PHOTO = "/api/photo/delete";
    public static final String ALBUM_LIKE_PHOTO = "/api/photo/like";
    public static final String ALBUM_PHOTO_LIKE_USER = "/api/photo/photoLikeUser";
    public static final String ALBUM_PHOTO_NOTIFY = "/api/message/sendPhotoNotify";

    //User interest
    public static final String INTEREST_SET_INTEREST = "/api/interest/settingInterest";
    public static final String INTEREST_GET_INTEREST = "/api/interest/userInterest";
    public static final String INTEREST_CREATE_INTEREST = "/api/interest/createType";
    public static final String INTEREST_SEND_INTERACT = "/api/message/sendInteract";

    //User recent_visitor
    public static final String RECENT_VISITOR = "/api/user/homeVistor";

    // For user confide
    public static final String CONFIDE_ROLE_LIST = "/api/confide/roleList";
    public static final String CONFIDE_COMMENT_LIST = "/api/confide/commentList";
    public static final String CONFIDE_LIST = "/api/confide/list";
    public static final String CONFIDE_PUBLISH = "/api/confide/post";
    public static final String CONFIDE_COMMENT = "/api/confide/comment";
    public static final String CONFIDE_BATCH_PRAISE = "/api/confide/batchPraise";
    public static final String CONFIDE_MY_CONFIDE = "/api/confide/myConfide";
    public static final String CONFIDE_DELETE = "/api/confide/delete";
    public static final String CONFIDE_PRAISE = "/api/confide/praise";

    //Chat Background
    public static final String CHAT_BACKGROUND_LIST = "/api/chat/backgroundList";

    //Mood
    public static final String MOOD_SEND = "/api/message/sendMood";

    //Music search
    public static final String MUSIC_SEARCH="/api/thirdMusic/search";
}

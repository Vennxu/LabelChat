package com.ekuater.labelchat.command;

/**
 * @author LinYong
 */
public final class CommandFields {

    public static final class Base {

        // Base command request parameters
        public static final String CLIENT_TYPE = "clientType";
        public static final String INTERFACE_VERSION = "interfaceVersion";
        // Account logon session
        public static final String SESSION = "sessionID";

        // Base command request result JSON field.
        public static final String STATE = "state";
        public static final String ERROR_DESC = "errorDesc";
        public static final String ERROR_CODE = "errorCode";

        // Default values
        public static final String DEFAULT_CLIENT_TYPE = "1";
        public static final String DEFAULT_INTERFACE_VERSION = "1";

        public static final String STATE_SUCCESS = "success";
        @SuppressWarnings("UnusedDeclaration")
        public static final String STATE_FAILED = "failure";
    }

    public static final class User {

        public static final String USER = "userVO";
        public static final String USERS = "userArray";
        public static final String USER_ID = "userId";
        public static final String LABEL_CODE = "labelCode";
        public static final String PASSWORD = "password";
        public static final String EMAIL = "email";
        public static final String MOBILE = "mobile";
        public static final String NICKNAME = "nickName";
        public static final String LAST_LOGIN_DATE = "lastLoginDate";
        public static final String BIRTHDAY = "birthday";
        public static final String AGE = "age";
        public static final String PROVINCE = "province";
        public static final String CITY = "city";
        public static final String SEX = "sex";
        public static final String CONSTELLATION = "constellation";
        public static final String SCHOOL = "school";
        public static final String AVATAR = "avata";
        public static final String AVATAR_THUMB = "avataThumb";
        public static final String SIGNATURE = "signature";
        public static final String POSITION = "position";
        public static final String LONGITUDE = "longitude";
        public static final String LATITUDE = "latitude";
        public static final String DISTANCE = "distance"; // meter
        public static final String APPEARANCE_FACE = "face";
        public static final String THEME = "theme";
        public static final String DATE = "date";
        public static final String VISITED = "homeVistorCount";
        public static final String HEIGHT = "height";
        public static final String JOB = "job";

        public static final String LABELS = UserLabel.LABELS;
    }

    public static final class Contact {

        public static final String CONTACT = User.USER;
        public static final String CONTACTS = User.USERS;
        public static final String USER_ID = User.USER_ID;
        public static final String LABEL_CODE = User.LABEL_CODE;
        public static final String NICKNAME = User.NICKNAME;
        public static final String MOBILE = User.MOBILE;
        public static final String REMARK = "friendRemark";
        public static final String AVATAR = User.AVATAR;
        public static final String AVATAR_THUMB = User.AVATAR_THUMB;
        public static final String POSITION = User.POSITION;
        public static final String SEX = User.SEX;
        public static final String BIRTHDAY = User.BIRTHDAY;
        public static final String AGE = User.AGE;
        public static final String CONSTELLATION = User.CONSTELLATION;
        public static final String PROVINCE = User.PROVINCE;
        public static final String CITY = User.CITY;
        public static final String SCHOOL = User.SCHOOL;
        public static final String SIGNATURE = User.SIGNATURE;
        public static final String APPEARANCE_FACE = User.APPEARANCE_FACE;
        public static final String THEME = User.THEME;
        public static final String HEIGHT = User.HEIGHT;
        public static final String JOB = User.JOB;

        public static final String LABELS = BaseLabel.LABELS;
    }

    public static final class Stranger {

        public static final String STRANGER = User.USER;
        public static final String STRANGERS = User.USERS;
        public static final String USER_ID = User.USER_ID;
        public static final String LABEL_CODE = User.LABEL_CODE;
        public static final String NICKNAME = User.NICKNAME;
        public static final String MOBILE = User.MOBILE;
        public static final String AVATAR = User.AVATAR;
        public static final String AVATAR_THUMB = User.AVATAR_THUMB;
        public static final String POSITION = User.POSITION;
        public static final String SEX = User.SEX;
        public static final String POP_USER = "popUser";
        public static final String BIRTHDAY = User.BIRTHDAY;
        public static final String AGE = User.AGE;
        public static final String CONSTELLATION = User.CONSTELLATION;
        public static final String PROVINCE = User.PROVINCE;
        public static final String CITY = User.CITY;
        public static final String SCHOOL = User.SCHOOL;
        public static final String SIGNATURE = User.SIGNATURE;
        public static final String THEME = User.THEME;
        public static final String STRANGER_USER_ID = "strangerUserId";
        public static final String HEIGHT = User.HEIGHT;
        public static final String JOB = User.JOB;


        public static final String LABELS = BaseLabel.LABELS;
    }

    public static final class Label {

        public static final String BASE_LABELS = "labelArray";
        public static final String USER_LABELS = "labelArray";
        public static final String SYSTEM_LABELS = "labelArray";

        public static final String LABEL_NAME = "labelName";
        public static final String LABEL_ID = "labelId";
        public static final String ADD_TIME = "addDate";
        public static final String CREATE_TIME = "createDate";
        public static final String CREATE_USER_ID = "createUserId";
        public static final String TOTAL_USER = "userTotal";
        public static final String PRAISE_COUNT = "praise";
        public static final String INTEGRAL = "integral";
        public static final String LABEL_PRAISES = "labelPraiseArray";
        public static final String FEED_USER = "feedUserVO";
        public static final String IMAGE = "labelImg";
        public static final String GRADE_NUM = "gradeNum";
        public static final String GRADE_TOTAL = "gradeTotal";
    }

    public static final class UserLabel {

        public static final String LABELS = Label.USER_LABELS;

        public static final String LABEL_NAME = Label.LABEL_NAME;
        public static final String LABEL_ID = Label.LABEL_ID;
        public static final String ADD_TIME = Label.ADD_TIME;
        public static final String TOTAL_USER = Label.TOTAL_USER;
        public static final String PRAISE_COUNT = Label.PRAISE_COUNT;
        public static final String INTEGRAL = Label.INTEGRAL;
        public static final String LABEL_PRAISES = Label.LABEL_PRAISES;
        public static final String FEED_USER = Label.FEED_USER;
        public static final String IMAGE = Label.IMAGE;
        public static final String GRADE_NUM = Label.GRADE_NUM;
        public static final String GRADE_TOTAL = Label.GRADE_TOTAL;
    }

    public static final class SystemLabel {

        public static final String LABELS = Label.SYSTEM_LABELS;

        public static final String LABEL_NAME = Label.LABEL_NAME;
        public static final String LABEL_ID = Label.LABEL_ID;
        public static final String CREATE_USER_ID = Label.CREATE_USER_ID;
        public static final String CREATE_TIME = Label.CREATE_TIME;
        public static final String TOTAL_USER = Label.TOTAL_USER;
        public static final String IMAGE = Label.IMAGE;
    }

    public static final class BaseLabel {

        public static final String LABELS = Label.BASE_LABELS;

        public static final String LABEL_NAME = Label.LABEL_NAME;
        public static final String LABEL_ID = Label.LABEL_ID;
    }

    public static final class Normal {

        // Parameter fields
        public static final String DEVICE_ID = "loginDeviceUUID";
        public static final String DEVICE_NAME = "deviceName";
        public static final String DEVICE_OS_VERSION = "deviceOSVersion";
        public static final String LOGIN_TEXT = "loginText";
        public static final String OLD_PASSWORD = "oldPassWord";
        public static final String NEW_PASSWORD = "newPassWord";
        public static final String REQUEST_USER_ID = "requestUserId";
        public static final String REQUEST_LABEL_CODE = "requestLabelCode";
        public static final String REQUEST_NICK_NAME = "requestNickName";
        public static final String VERIFY_MSG = "verifyMsg";
        public static final String KEYWORD = "keyword";
        public static final String REQUEST_TIME = "requestTime";
        public static final String WEEKLY_STAR_SESSION = "session";
        public static final String ACCEPT = "accept";
        public static final String FRIEND_USER_ID = "friendUserId";
        public static final String FRIEND_LABEL_CODE = "friendLabelCode";
        public static final String FRIEND_REMARK = "friendRemark";
        public static final String QUERY_USER_ID = "queryUserId";
        public static final String STRANGER_USER_ID = "strangerUserId";

        public static final String VERIFY_TYPE = "verifyType";
        public static final String VERIFY_TYPE_AGREE = "agree";
        public static final String VERIFY_TYPE_REJECT = "reject";
        public static final String REJECT_MSG = "rejectMsg";

        public static final String SCENARIO = "scenario";
        public static final String SCENARIO_REGISTER = "register";
        public static final String SCENARIO_MODIFY_PASSWORD = "modifyPassword";
        public static final String SCENARIO_BIND_MOBILE = "bindMobile";

        public static final String SCENARIO_PICK = "pick";
        public static final String SCENARIO_BROWSE = "browse";

        public static final String MOBILE = "mobile";
        public static final String CAPTCHA = "captcha";
        public static final String SEARCH_WORD = "searchWord";
        public static final String PUSH_ID = "pushId";
        public static final String CONTENT = "content";

        public static final String SUGGESTION = "suggestion";
        public static final String CONTACT_INFO = "contact";

        public static final String TOKEN = "token";

        public static final String RAND_COUNT = "randCount";

        public static final String FILE = "file";

        public static final String SHARE_PLATFORM = "sharePlatform";

        public static final String COMPLAIN_TYPE = "complainType";
        public static final String OBJECT_ID = "objectId";
        public static final String COMPLAIN_TYPE_USER = "USER";
        public static final String COMPLAIN_TYPE_DYNAMIC = "DYNAMIC";
        public static final String COMPLAIN_TYPE_CONFIDE = "CONFIDE";

        // Result fields
        public static final String ENCRYPTION = "encryption";
        public static final String CERTIFICATE = "certificate";
        public static final String REMAINING = "remaining";
    }

    public static final class OAuth {

        public static final String PLATFORM = "platform";
        public static final String OPEN_ID = "openid";
        public static final String ACCESS_TOKEN = "accessToken";
        public static final String TOKEN_EXPIRE = "tokenExpirein";
    }

    public static final class TmpGroup {

        public static final String GROUP = "groupVO";
        public static final String REASON = "reason";
        public static final String LABEL_ARRAY = BaseLabel.LABELS;
        public static final String GROUP_ID = "groupId";
        public static final String GROUP_NAME = "groupName";
        public static final String LABEL = "label";
        public static final String CREATE_USER_ID = "creatorUserId";
        public static final String CREATE_TIME = "createTime";
        public static final String EXPIRE_TIME = "expireTime";
        public static final String SYSTEM_TIME = "systemTime";
        public static final String GROUP_AVATAR = "groupAvata";
        public static final String STATE = "state";
        public static final String USER_ARRAY = "userArray";
    }

    public static final class StoryLabel {

        public static final String LABEL_STORY_ID = "labelStoryId";

        public static final String STORY_CONTENT = "content";
        public static final String TYPE = "type";
        public static final String MEDIA = "media";
        public static final String DURATION = "duration";
        public static final String MEDIAURL = "mediaUrl";
        public static final String SINGERPIC = "singerPic";
        public static final String PRAISE = "praise";
        public static final String CREATE_DATE = "createDate";
        public static final String MODIFY_DATE = "modifyDate";
        public static final String AUTHOR_USER_ID = "authorUserId";
        public static final String ISPRAISE = "isPraise";
        public static final String ISFOLLOWING = "isFollowing";
        public static final String LABEL_STORY_FLOOR = "floor";
        public static final String LABEL_STORY_IMGS = "storyImgs";
        public static final String LABEL_STORY_IMG_THUMBS = "storyImgThumbs";
        public static final String LABEL_STORY_USER_VO = "userVO";

        public static final String LABEL_STORY_ARRAY = "labelStoryArray";
        public static final String REQUEST_TIME = Normal.REQUEST_TIME;
        public static final String QUERY_USER_ID = Normal.QUERY_USER_ID;

        public static final String LABEL_STORY_VO = "labelStoryVO";
        public static final String STORY_COMMENT_ARRAY = "storyCommentArray";
        public static final String STORY_COMMENT_ID = "storyCommentId";
        public static final String STORY_COMMENT = "storyComment";
        public static final String COMMENT_NUM = "commentNum";
        public static final String SECRET_NUM = "secretNum";
        public static final String FRIEND_STRORY_COUNT = "friendStroryCount";

        public static final String LABEL_VO = "labelVO";
        public static final String PRAISE_USER_ARRAY = "praiseUserArray";

        public static final String PARENT_COMMENT_ID = "parentCommentId";
        public static final String REPLY_NICK_NAME = "replyNickName";
        public static final String LABEL_STORY_COMMENT_VO = "labelStoryCommentVO";
        public static final String BROWSE_NUM = "browseNum";
        public static final String BROWER_USER_ARRAY = "browerUserArray";

        public static final String REPLY_USER_ID = "replyUserId";
        public static final String RELATED_USER_IDS = "relatedUserIds";

        public static final String CATEGORY_VO = "categoryVO";
        public static final String CATEGORY_ID = "categoryId";
        public static final String CATEGORY_NAME = "categoryName";
        public static final String DYNAMIC_TOTAL = "dynamicTotal";
        public static final String SERIAL_NUM = "serialNum";
        public static final String DYNAMIC_CATEGORY_ARRAY = "dynamicCategoryArray";

        public static final String MESSAGE = "message";
        public static final String TAG = "tag";

        public static final String CATEGORY_IMG = "categoryImg";
        public static final String MY_STORY_TOTAL = "myStoryTotal";

        public static final String TYPE_TXT_IMG = "1";
        public static final String TYPE_AUDIO = "2";
        public static final String TYPE_VIDEO = "3";
        public static final String TYPE_BANKNOTE = "4";
        public static final String TYPE_ONLINEAUDIO = "5";
    }

    public static final class Dynamic {

        public static final String DYNAMIC_OPERATE = "operate";
        public static final String DYNAMIC__NOTIFY_MESSAGE_PLACE = "messagePlace";
        public static final String DYNAMIC_USER_INFO_VO = "userVO";
        public static final String DYNAMIC_INFO_VO = "labelStoryVO";
        public static final String DYNAMIC_COMMENT_INFO_VO = "storyCommentVO";
        public static final String DYNAMIC__ID = "labelStoryId";
        public static final String NICKNAME = "nickName";
        public static final String DYNAMIC_CREATE_DATE = "createDate";
        public static final String DYNAMIC_CONTENT = "content";
        public static final String DYNAMIC_IMG = "storyImgs";
        public static final String DYNAMIC_IMG_THUMB = "storyImgThumbs";

        public static final String DYNAMIC_COMMENT_ID = "storyCommentId";
        public static final String COMMENT_ID = "commentId";
        public static final String DYNAMIC_COMMENT_CONTENT = "storyComment";
        public static final String DYNAMIC_REPLY_COMMENT_CONTENT = "replyStoryComment";
        public static final String DYNAMIC_USER_ID = "userId";
        public static final String DYNAMIC_REPLY_NICKNAME = "replyNickName";

        public static final String DYNAMIC_ARRAY = "dynamicArray";
        public static final String DYNAMIC_COMMENT_ARRAY = "dynamicCommentArray";
        public static final String CONTENT_TYPE = "contentType";
        public static final String OBJECT_ID = "objectId";

        public static final String FILTERS = "filters";
        public static final String FILTER_TYPE = "type";
        public static final String FILTER_SUBTYPE = "subtype";
        public static final String DYNAMIC_TYPE = FILTER_TYPE;

        public static final String CONTENT_TYPE_STORY = "1";
        public static final String CONTENT_TYPE_CONFIDE = "2";

        public static final String MUSIC_VO = "musicVO";
        public static final String MUSIC_ARRAY = "musicArray";
        public static final String SONG_ID = "songId";
        public static final String SINGER_ID = "singerId";
        public static final String SONG_NAME = "songName";
        public static final String SINGER_NAME = "singerName";
        public static final String ALBUM_NAME = "albumName";
        public static final String MUSIC_URL = "musicUrl";
        public static final String DURATION = "duration";
        public static final String KEY_WORD = "q";
        public static final String SINGER_PIC="singerPic";



    }

    public static final class ThrowPhoto {

        public static final String THROW_PHOTO = "throwPhotoVO";
        public static final String THROW_PHOTO_ARRAY = "throwPhotoArray";
        public static final String THROW_PHOTO_ID = "throwPhotoId";
        public static final String USER_ID = User.USER_ID;
        public static final String THROW_DATE = "throwDate";
        public static final String LONGITUDE = User.LONGITUDE;
        public static final String LATITUDE = User.LATITUDE;
        public static final String PICK_TOTAL = "pickTotal";
        public static final String DISPLAY_PHOTO = "displayPhoto";
        public static final String PHOTO_ARRAY = "photoItemArray";

        public static final String PHOTO_ID = "throwPhotoItemId";
        public static final String PHOTO = "photo";
        public static final String PHOTO_THUMB = "photoThumb";
        public static final String PHOTO_CHECK = "pickUserArray";
        public static final String PICK_PHOTO_DATE = "createDate";
    }

    public static final class Theme {

        public static final String THEME = User.THEME;
        public static final String THEMES = "themeArray";
        public static final String CHAT_BG_ARRAY = "chatBgArray";
        public static final String ID = "id";
        public static final String BG_IMG = "bgImg";
        public static final String BG_THUMB = "bgThumb";
        public static final String SERIAL_NUM = "serialNum";
    }

    public static final class ChatRoom {

        public static final String CHAT_ROOM_ARRAY = "chatRoomArray";
        public static final String CHAT_ROOM_ID = "chatRoomId";
        public static final String CHAT_ROOM_NAME = "chatRoomName";
        public static final String MEMBER_COUNT = "onlineCount";
        public static final String CHAT_ROOM_IMG = "chatRoomImg";
        public static final String CHAT_ROOM_DESC = "chatRoomDesc";
    }

    public static final class Following {

        public static final String FOLLOWING_IS = "isFollowing";
        public static final String FOLLOWING_COUNT = "followingCount";
        public static final String FOLLOWER_COUNT = "followerCount";
        public static final String FOLLOW_USER_ID = "followUserId";
        public static final String INVITE_USER_ID = "inviteUserId";

        public static final String FOLLOWER_USER_ID = "followerUserId";
    }

    public static final class Tag {

        public static final String TAG_TYPE_ARRAY = "tagTypeArray";
        public static final String TYPE_ID = "typeId";
        public static final String TYPE_NAME = "typeName";
        public static final String MAX_SELECT = "maxSelect";
        public static final String TAG_ARRAY = "tagArray";
        public static final String TAG_ID_ARRAY = "tagIdArray";
        public static final String TAG_ID = "tagId";
        public static final String TAG_NAME = "tagName";
        public static final String TAG_COLOR = "color";
        public static final String TAG_SELECTED_COLOR = "selectColor";
        public static final String ADD_TAG_ID = "addTagId";
    }

    public static final class Album {

        public static final String PHOTO_VO = "photoVO";
        public static final String PHOTO_ARRAY = "photoArray";
        public static final String PHOTO_ID = "photoId";
        public static final String USER_ID = User.USER_ID;
        public static final String CREATE_DATE = "createDate";
        public static final String PHOTO = "photo";
        public static final String PHOTO_THUMB = "photoThumb";
        public static final String IS_LIKE = "isLike";
        public static final String IS_SAW = "isNotifyIsee";
        public static final String IS_REMINDED = "isNotifyAgainUpload";
        public static final String YES = "Y";
        public static final String LIKE_NUM = "likeNum";
        public static final String NOTIFY_UPLOAD_NUM = "notifyUploadNum";

        public static final String PHOTO_USER_ID = "photoUserId";
        public static final String PHOTO_NOTIFY_TYPE = "photoNotifyType";
        public static final String RELATED_USER_ID = "relatedUserIds";
        public static final String MY_PHOTO_TOTAL = "myPhotoTotal";
    }

    public static final class Interest {

        public static final String INTEREST_TYPE_ARRAY = "interestTypeArray";
        public static final String INTEREST_TYPE_ID = "typeId";
        public static final String INTEREST_TYPE_NAME = "typeName";
        public static final String INTEREST_ARRAY = "interestArray";
        public static final String INTEREST_ID = "interestId";
        public static final String INTEREST_NAME = "interest";

        public static final String ADD_INTEREST = "addInterest";
        public static final String INTERACT_TYPE = "interactType";
        public static final String INTERACT_USERID = "interactUserId";
        public static final String INTERACT_OBJECT = "interactObject";
        public static final String INTERACT_OPERATE = "interactOperate";

        public static final String OBJECT_TYPE = "objectType";
        public static final String SELECT_COLOR = "selectColor";
    }

    public static final class Confide {

        public static final String CONFIDE_VO = "confideVO";
        public static final String CONFIDE_ARRAY = "confideArray";
        public static final String CONFIDE_ID_ARRAY = "confideIdArray";
        public static final String ROLE_ARRAY = "confideRoleArray";
        public static final String CONFIDE_COMMENT_VO = "confideCommentVO";

        public static final String ROLE_ID = "id";
        public static final String ROLE = "role";
        public static final String CONTENT = "content";
        public static final String BG_COLOR = "bgColor";
        public static final String BG_IMG = "bgImg";
        public static final String SEX = User.SEX;
        public static final String POSITION = User.POSITION;

        public static final String CONFIDE_ID = "confideId";
        public static final String CONFIDE_USER_ID = User.USER_ID;
        public static final String CONFIDE_SEX = User.SEX;
        public static final String CONFIDE_ROLE = ROLE;
        public static final String CONFIDE_POSITION = User.POSITION;
        public static final String CONFIDE_BG_COLOR = "bgColor";
        public static final String CONFIDE_CONTENT = StoryLabel.STORY_CONTENT;
        public static final String CONFIDE_PRAISE_NUM = "praiseNum";
        public static final String CONFIDE_COMMENT_NUM = "commentNum";
        public static final String CONFIDE_IS_PRAISE = StoryLabel.ISPRAISE;
        public static final String CONFIDE_DATE = StoryLabel.CREATE_DATE;
        public static final String CONFIDE_COMMENT_ARRAY = "confideCommentArray";
        public static final String CONFIDE_COMMENT_ID = "confideCommentId";
        public static final String CONFIDE_COMMENT = "comment";
        public static final String CONFIDE_COMMENT_FLOOR = StoryLabel.LABEL_STORY_FLOOR;
        public static final String CONFIDE_VIRTUAL_AVATAR = "virtualAvata";

        public static final String CONFIDE_REPLY_COMMENT = "replyComment";
        public static final String CONFIDE_REPLY_FLOOR = "replyFloor";
        public static final String CONFIDE_COMMENT_PARENT = StoryLabel.PARENT_COMMENT_ID;
        public static final String CONFIDE_OPERATE = "operate";
        public static final String CONFIDE_MESSAGE_PLACE = "messagePlace";
        public static final String CONFIDE_USER_VO = "userVO";
        public static final String CONFIDE_COMMENT_RELATED_USER_IDS = StoryLabel.RELATED_USER_IDS;
    }

    public static final class Mood {
        public static final String MOOD = "mood";
        public static final String RELATED_USER_IDS = "relatedUserIds";
    }
}

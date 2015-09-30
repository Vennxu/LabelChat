
package com.ekuater.labelchat.data;

import android.net.Uri;
import android.provider.BaseColumns;

import com.ekuater.labelchat.BuildConfig;

/**
 * @author LinYong
 */
public final class DataConstants {

    public static final class Chat implements BaseColumns {

        public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".data.ChatProvider";
        public static final String PATH = "chats";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ekuater.labelchat.chat";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ekuater.labelchat.chat";

        // Columns
        public static final String TARGET_ID = "userId"; // friend userId, will be groupId in group chat
        public static final String SENDER_ID = "sender_id"; // for group chat
        public static final String CONVERSATION = "conversation";
        public static final String MESSAGE_ID = "message_id";
        public static final String DIRECTION = "direction"; // received or sent
        public static final String DATETIME = "datetime";
        public static final String STATE = "state";
        public static final String TYPE = "type";
        public static final String CONTENT = "content";
        public static final String PREVIEW = "preview";

        public static final String[] REQUIRED_COLUMNS = new String[]{
                TARGET_ID,
                DIRECTION,
                DATETIME,
                STATE,
                TYPE,
        };

        public static final String[] ALL_COLUMNS = new String[]{
                _ID,
                TARGET_ID,
                SENDER_ID,
                CONVERSATION,
                MESSAGE_ID,
                DIRECTION,
                DATETIME,
                STATE,
                TYPE,
                CONTENT,
                PREVIEW,
        };
    }

    public static final class Contact implements BaseColumns {

        public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".data.ContactProvider";
        public static final String PATH = "contacts";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ekuater.labelchat.contact";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ekuater.labelchat.contact";

        // Columns
        public static final String USER_ID = "userId";
        public static final String LABEL_CODE = "label_code";
        public static final String NICKNAME = "nick_name";
        public static final String REMARKS_NAME = "remarks_name";
        public static final String MOBILE = "mobile";
        public static final String SEX = "sex";
        public static final String BIRTHDAY = "birthday";
        public static final String AGE = "age";
        public static final String CONSTELLATION = "constellation";
        public static final String PROVINCE = "province";
        public static final String CITY = "city";
        public static final String SCHOOL = "school";
        public static final String SIGNATURE = "signature";
        public static final String AVATAR = "avatar";
        public static final String AVATAR_THUMB = "avatar_thumb";
        public static final String LABELS = "labels";
        public static final String APPEARANCE_FACE = "face";
        public static final String THEME = "theme";

        public static final String[] REQUIRED_COLUMNS = new String[]{
                USER_ID,
                LABEL_CODE,
        };

        public static final String[] ALL_COLUMNS = new String[]{
                _ID,
                USER_ID,
                LABEL_CODE,
                NICKNAME,
                REMARKS_NAME,
                MOBILE,
                SEX,
                BIRTHDAY,
                AGE,
                CONSTELLATION,
                PROVINCE,
                CITY,
                SCHOOL,
                SIGNATURE,
                AVATAR,
                AVATAR_THUMB,
                LABELS,
                APPEARANCE_FACE,
                THEME,
        };
    }

    public static final class Push implements BaseColumns {

        public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".data.SystemPushProvider";
        public static final String PATH = "systempush";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ekuater.labelchat.systempush";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ekuater.labelchat.systempush";

        // Columns
        public static final String TYPE = "type";
        public static final String DATETIME = "datetime";
        public static final String STATE = "state";
        public static final String CONTENT = "content";
        public static final String FLAGS = "flags";

        public static final String[] REQUIRED_COLUMNS = new String[]{
                TYPE,
                DATETIME,
                STATE,
                CONTENT,
                FLAGS,
        };

        public static final String[] ALL_COLUMNS = new String[]{
                _ID,
                TYPE,
                DATETIME,
                STATE,
                CONTENT,
                FLAGS,
        };
    }
}

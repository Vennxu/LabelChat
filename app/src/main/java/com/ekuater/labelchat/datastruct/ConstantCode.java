
package com.ekuater.labelchat.datastruct;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.CommandFields;

/**
 * @author LinYong
 */
public final class ConstantCode {

    // Request command request method
    // Always user POST now
    public static final int REQUEST_GET = 0;
    public static final int REQUEST_POST = 1;
    public static final int REQUEST_PUT = 2;
    public static final int REQUEST_DELETE = 3;

    // for user sex enum
    public static final int USER_SEX_UNKNOWN = 0;
    public static final int USER_SEX_MALE = 1;
    public static final int USER_SEX_FEMALE = 2;
    public static final int USER_SEX_SECRECY = 3;

    public static int getSexImageResource(int sex) {
        int resId;

        switch (sex) {
            case USER_SEX_UNKNOWN:
                resId = R.drawable.icon_male;
                break;
            case USER_SEX_MALE:
                resId = R.drawable.icon_male;
                break;
            case USER_SEX_FEMALE:
                resId = R.drawable.icon_female;
                break;
            case USER_SEX_SECRECY:
                resId = R.drawable.icon_male;
                break;
            default:
                resId = R.drawable.icon_male;
                break;
        }

        return resId;
    }

    public static int getSexStringResource(int sex) {
        int resId;

        switch (sex) {
            case USER_SEX_UNKNOWN:
                resId = R.string.unknown;
                break;
            case USER_SEX_MALE:
                resId = R.string.he;
                break;
            case USER_SEX_FEMALE:
                resId = R.string.her;
                break;
            case USER_SEX_SECRECY:
                resId = R.string.he;
                break;
            default:
                resId = R.string.he;
                break;
        }

        return resId;
    }

    // for auth type, normal or oauth
    public static final int AUTH_TYPE_NORMAL = 0;
    public static final int AUTH_TYPE_OAUTH = 1;
    // for OAuth platform
    public static final String OAUTH_PLATFORM_QQ = "QQ";
    public static final String OAUTH_PLATFORM_SINA_WEIBO = "SinaWeibo";
    public static final String OAUTH_PLATFORM_WEIXIN = "WeiXin";

    // update personal information items
    public static final String PERSONAL_INFO_AVATAR = CommandFields.User.AVATAR;
    public static final String PERSONAL_INFO_NICK_NAME = CommandFields.User.NICKNAME;
    public static final String PERSONAL_INFO_SEX = CommandFields.User.SEX;
    public static final String PERSONAL_INFO_PROVINCE = CommandFields.User.PROVINCE;
    public static final String PERSONAL_INFO_CITY = CommandFields.User.CITY;
    public static final String PERSONAL_INFO_CONSTELLATION = CommandFields.User.CONSTELLATION;
    public static final String PERSONAL_INFO_SCHOOL = CommandFields.User.SCHOOL;
    public static final String PERSONAL_INFO_SIGNATURE = CommandFields.User.SIGNATURE;

    // update personal information item array
    public static final String[] PERSONAL_UPDATE_INFO_ARRAY = {
            PERSONAL_INFO_AVATAR,
            PERSONAL_INFO_NICK_NAME,
            PERSONAL_INFO_SEX,
            PERSONAL_INFO_PROVINCE,
            PERSONAL_INFO_CITY,
            PERSONAL_INFO_CONSTELLATION,
            PERSONAL_INFO_SCHOOL,
            PERSONAL_INFO_SIGNATURE,
    };

    // command execute code
    public static final int EXECUTE_RESULT_SUCCESS = 0;
    public static final int EXECUTE_RESULT_EMPTY_CMD = 1;
    public static final int EXECUTE_RESULT_EMPTY_PARAM = 2;
    public static final int EXECUTE_RESULT_NETWORK_ERROR = 3;

    // IM connect result code
    public static final int IM_CONNECT_SUCCESS = 0;
    public static final int IM_CONNECT_NETWORK_ERROR = 1;
    public static final int IM_CONNECT_AUTHENTICATE_FAILED = 2;
    public static final int IM_CONNECT_PARAMETER_ERROR = 3;

    // chat message send result code
    public static final int SEND_RESULT_SUCCESS = 0;
    public static final int SEND_RESULT_NETWORK_ERROR = 1;
    public static final int SEND_RESULT_CONNECTION_CLOSE = 2;
    public static final int SEND_RESULT_EMPTY_MESSAGE = 101;
    public static final int SEND_RESULT_OFFLINE = 102;

    // for command operation result code
    // for common operation result code
    public static final int COMMAND_OPERATION_SUCCESS = 0;
    public static final int COMMAND_OPERATION_SESSION_ID_INVALID = 1;
    public static final int COMMAND_OPERATION_SYSTEM_ERROR = 2;
    public static final int COMMAND_OPERATION_NETWORK_ERROR = 3;

    // account about operation result code
    public static final int ACCOUNT_OPERATION_SUCCESS = COMMAND_OPERATION_SUCCESS;
    public static final int ACCOUNT_OPERATION_NETWORK_ERROR = 201;
    public static final int ACCOUNT_OPERATION_SYSTEM_ERROR = 202;
    public static final int ACCOUNT_OPERATION_RESPONSE_DATA_ERROR = 203;
    public static final int ACCOUNT_OPERATION_USER_OR_PASSWORD_ERROR = 204;
    public static final int ACCOUNT_OPERATION_DATA_ALREADY_EXIST = 205;
    public static final int ACCOUNT_OPERATION_NO_DATA = 206;
    public static final int ACCOUNT_OPERATION_AUTHORIZE_FAILURE = 207;
    public static final int ACCOUNT_OPERATION_USER_NOT_EXIST = 208;
    public static final int ACCOUNT_OPERATION_ILLEGAL_PASSWORD = 209;
    public static final int ACCOUNT_OPERATION_DO_NOT_NEED = 210;
    public static final int ACCOUNT_OPERATION_VERIFY_CODE_EXPIRED = 211;
    public static final int ACCOUNT_OPERATION_VERIFY_CODE_WRONG = 212;
    public static final int ACCOUNT_OPERATION_MOBILE_ALREADY_EXIST = 213;

    // label about operation result code
    public static final int LABEL_OPERATION_SUCCESS = COMMAND_OPERATION_SUCCESS;
    public static final int LABEL_OPERATION_NETWORK_ERROR = 401;
    public static final int LABEL_OPERATION_RESPONSE_DATA_ERROR = 402;
    public static final int LABEL_OPERATION_SYSTEM_ERROR = 403;
    public static final int LABEL_OPERATION_LABEL_IN_USE = 404;

    // contact operation result code
    public static final int CONTACT_OPERATION_SUCCESS = COMMAND_OPERATION_SUCCESS;
    public static final int CONTACT_OPERATION_NETWORK_ERROR = 601;
    public static final int CONTACT_OPERATION_RESPONSE_DATA_ERROR = 602;
    public static final int CONTACT_OPERATION_SYSTEM_ERROR = 603;
    // end for command operation result code

    // for tmp group
    public static final int TMP_GROUP_OPERATION_SUCCESS = COMMAND_OPERATION_SUCCESS;
    public static final int TMP_GROUP_OPERATION_SESSION_INVALID = COMMAND_OPERATION_SESSION_ID_INVALID;
    public static final int TMP_GROUP_OPERATION_SYSTEM_ERROR = COMMAND_OPERATION_SYSTEM_ERROR;
    public static final int TMP_GROUP_OPERATION_NETWORK_ERROR = 701;
    public static final int TMP_GROUP_OPERATION_RESPONSE_DATA_ERROR = 702;
    public static final int TMP_GROUP_OPERATION_EMPTY_PARAM = 703;
    public static final int TMP_GROUP_OPERATION_GROUP_EXIST = 704;
    public static final int TMP_GROUP_OPERATION_DATA_NOT_EXIST = 705;
    public static final int TMP_GROUP_OPERATION_GROUP_DISMISSED = 706;

    public static final int TAG_OPERATION_SUCCESS = COMMAND_OPERATION_SUCCESS;
    public static final int TAG_OPERATION_NETWORK_ERROR = COMMAND_OPERATION_NETWORK_ERROR;
    public static final int TAG_OPERATION_RESPONSE_DATA_ERROR = 801;
    public static final int TAG_OPERATION_SYSTEM_ERROR = 802;
    public static final int TAG_OPERATION_LABEL_IN_USE = 803;
}

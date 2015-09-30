package com.ekuater.labelchat.command;

/**
 * @author LinYong
 */
public final class CommandErrorCode {

    // public error code
    public static final int EXECUTE_FAILED = -1;
    public static final int REQUEST_SUCCESS = 200;
    public static final int SESSION_ID_INVALID = 403;
    public static final int SYSTEM_ERROR = 9999;

    public static final int PARAM_EMPTY = 1001;
    public static final int NO_DATA = 1002;
    public static final int USER_OR_PASSWORD_ERROR = 1004;
    public static final int AUTHORIZE_FAILURE = 1005;
    public static final int USER_NOT_EXIST = 1006;
    public static final int ILLEGAL_PASSWORD = 1007;

    // for request verify code
    public static final int VERIFY_CODE_NOT_EXPIRED = 1003;
    public static final int MOBILE_NOT_EXIST = 1006;

    // for register
    public static final int DATA_ALREADY_EXIST = 1003;

    // for validate add friend
    public static final int ALREADY_VALIDATE_ADDED = 1003;

    // for bind third user account
    public static final int VERIFY_CODE_EXPIRED = 1011;
    public static final int VERIFY_CODE_WRONG = 1012;
    public static final int MOBILE_ALREADY_EXIST = 1013;

    public static final int DATA_NOT_EXIST = 1009;

    public static final int GROUP_DISMISSED = 1016;

    public static final int DATA_IN_USE = 1017;
}

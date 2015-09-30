package com.ekuater.labelchat.datastruct;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
public class WeeklyStarConfirmMessage {

    private static final String FIELD_LABEL_CODE = SystemPushFields.FIELD_LABEL_CODE;
    private static final String FIELD_MESSAGE = SystemPushFields.FIELD_MESSAGE;
    private static final String FIELD_SESSION = SystemPushFields.FIELD_SESSION;

    private final String mLabelCode;
    private final String mMessage;
    private final String mSession;

    public WeeklyStarConfirmMessage(String labelCode, String message, String session) {
        mLabelCode = labelCode;
        mMessage = message;
        mSession = session;
    }

    public String getLabelCode() {
        return mLabelCode;
    }

    public String getMessage() {
        return mMessage;
    }

    public String getSession() {
        return mSession;
    }

    public static WeeklyStarConfirmMessage build(JSONObject json) {
        WeeklyStarConfirmMessage confirmMsg = null;

        try {
            final String labelCode = json.getString(FIELD_LABEL_CODE);
            final String message = json.getString(FIELD_MESSAGE);
            final String session = json.getString(FIELD_SESSION);

            if (!TextUtils.isEmpty(labelCode)
                    && !TextUtils.isEmpty(message)
                    && !TextUtils.isEmpty(session)) {
                confirmMsg = new WeeklyStarConfirmMessage(labelCode, message, session);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return confirmMsg;
    }

    public static WeeklyStarConfirmMessage build(SystemPush push) {
        WeeklyStarConfirmMessage confirmMsg = null;

        if (push.getType() == SystemPushType.TYPE_CONFIRM_WEEKLY_STAR) {
            try {
                confirmMsg = build(new JSONObject(push.getContent()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return confirmMsg;
    }
}

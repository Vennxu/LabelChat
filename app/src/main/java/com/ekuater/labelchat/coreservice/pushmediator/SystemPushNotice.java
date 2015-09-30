package com.ekuater.labelchat.coreservice.pushmediator;

import android.text.TextUtils;

import com.ekuater.labelchat.command.CommandFields;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A system push message notice pushed from server,
 * and user this notice to pull the hole system push message from server.
 *
 * @author LinYong
 */
public class SystemPushNotice {

    private static final String AREA_KEY_PUSH_ID = CommandFields.Normal.PUSH_ID;

    // message id to identify the system push message on server.
    private final String mPushId;

    public SystemPushNotice(String pushId) {
        mPushId = pushId;
    }

    public String getPushId() {
        return mPushId;
    }

    public JSONObject toJson() {
        JSONObject json = null;

        try {
            json = new JSONObject();
            json.put(AREA_KEY_PUSH_ID, mPushId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    @Override
    public String toString() {
        return getPushId();
    }

    /**
     * Build a new SystemPushNotice from a String
     *
     * @param string SystemPushNotice String
     * @return a new SystemPushNotice, may be null when build failed.
     */
    public static SystemPushNotice build(String string) {
        SystemPushNotice notice = null;

        if (!TextUtils.isEmpty(string)) {
            try {
                notice = build(new JSONObject(string));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return notice;
    }

    /**
     * Build a new SystemPushNotice from a JSON object.
     *
     * @param json JSON object
     * @return a new SystemPushNotice, may be null when build failed.
     */
    public static SystemPushNotice build(JSONObject json) {
        SystemPushNotice notice = null;

        if (json != null) {
            try {
                String pushId = json.getString(AREA_KEY_PUSH_ID);
                notice = new SystemPushNotice(pushId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return notice;
    }
}

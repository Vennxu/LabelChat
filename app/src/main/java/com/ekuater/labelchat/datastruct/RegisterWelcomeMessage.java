package com.ekuater.labelchat.datastruct;

import com.ekuater.labelchat.util.L;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author LinYong
 */
public class RegisterWelcomeMessage extends LocalPushMessage {

    private static final String TAG = RegisterWelcomeMessage.class.getSimpleName();

    private String mMessage;
    private String mUrl;

    public RegisterWelcomeMessage() {
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String welcomeMessage) {
        mMessage = welcomeMessage;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    @Override
    public SystemPush toSystemPush() {
        try {
            JSONObject json = new JSONObject();
            json.put(SystemPushFields.FIELD_WELCOME_MESSAGE, getMessage());
            json.put(SystemPushFields.FIELD_WELCOME_URL, getUrl());

            SystemPush systemPush = new SystemPush();
            systemPush.setId(-1L);
            systemPush.setState(SystemPush.STATE_UNPROCESSED);
            systemPush.setType(SystemPushType.TYPE_REGISTER_WELCOME);
            systemPush.setTime(System.currentTimeMillis());
            systemPush.setContent(json.toString());

            return systemPush;
        } catch (JSONException e) {
            L.w(TAG, e);
            return null;
        }
    }

    public static RegisterWelcomeMessage build(JSONObject json) {
        RegisterWelcomeMessage newMessage = null;

        if (json != null) {
            try {
                String message = json.getString(SystemPushFields.FIELD_WELCOME_MESSAGE);
                String url = json.getString(SystemPushFields.FIELD_WELCOME_URL);
                newMessage = new RegisterWelcomeMessage();
                newMessage.setMessage(message);
                newMessage.setUrl(url);
            } catch (JSONException e) {
                L.w(TAG, e);
            }
        }

        return newMessage;
    }

    public static RegisterWelcomeMessage build(SystemPush push) {
        RegisterWelcomeMessage newMessage = null;

        if (push.getType() == SystemPushType.TYPE_REGISTER_WELCOME) {
            try {
                newMessage = build(new JSONObject(push.getContent()));
            } catch (JSONException e) {
                L.w(TAG, e);
            }
        }

        return newMessage;
    }
}

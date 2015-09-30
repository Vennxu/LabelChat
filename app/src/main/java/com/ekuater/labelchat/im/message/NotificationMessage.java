
package com.ekuater.labelchat.im.message;

import com.ekuater.labelchat.im.PacketStruct;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The system notification push message. format:{ "type": 12, "time": 54861235,
 * "content": { "content": "hello world!" }
 * 
 * @author LinYong
 */
public class NotificationMessage extends JsonMessage {

    private static final String TYPE_NAME = "type";
    private static final String TIME_NAME = "time";
    private static final String CONTENT_NAME = "content";

    public NotificationMessage() {
        super(PacketStruct.TYPE_NOTIFICATION);
    }

    public int getNotificationType() {
        int type = -1;

        try {
            type = getJson().getInt(TYPE_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return type;
    }

    public void setNotificationType(int type) {
        try {
            getJson().put(TYPE_NAME, type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public long getNotificationTime() {
        long time = 0L;

        try {
            time = getJson().getLong(TIME_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return time;
    }

    public void setNotificationTime(long time) {
        try {
            getJson().put(TIME_NAME, time);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getNotificationContent() {
        JSONObject content = new JSONObject();

        try {
            content = getJson().getJSONObject(CONTENT_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return content;
    }

    public void setNotificationContent(JSONObject content) {
        try {
            getJson().put(CONTENT_NAME, content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

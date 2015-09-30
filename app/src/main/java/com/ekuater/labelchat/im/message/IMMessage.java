
package com.ekuater.labelchat.im.message;

import com.ekuater.labelchat.im.PacketStruct;

import org.json.JSONException;

/**
 * IM chat message, the content of message will be a JSON object. JSON format: {
 * "type": 1, "time":123456789, "content": "We are friends.", "preview":
 * "ADEGSDBxGESGsSljsjiaoJLKSJ" }
 * 
 * @author LinYong
 */
public class IMMessage extends JsonMessage {

    private static final String TYPE_NAME = "type";
    private static final String TIME_NAME = "time";
    private static final String CONTENT_NAME = "content";
    private static final String PREVIEW_NAME = "preview";

    private static final String EMPTY_STRING = "";

    public IMMessage() {
        super(PacketStruct.TYPE_MESSAGE);
    }

    public int getChatMessageType() {
        int type = -1;

        try {
            type = getJson().getInt(TYPE_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return type;
    }

    public void setChatMessageType(int type) {
        try {
            getJson().put(TYPE_NAME, type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public long getChatMessageTime() {
        long time = 0L;

        try {
            time = getJson().getLong(TIME_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return time;
    }

    public void setChatMessageTime(long time) {
        try {
            getJson().put(TIME_NAME, time);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getChatMessageContent() {
        String content = EMPTY_STRING;

        try {
            content = getJson().getString(CONTENT_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return content;
    }

    public void setChatMessageContent(String content) {
        try {
            getJson().put(CONTENT_NAME, content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getChatMessagePreview() {
        String preview = EMPTY_STRING;

        try {
            preview = getJson().getString(PREVIEW_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return preview;
    }

    public void setChatMessagePreview(String preview) {
        try {
            getJson().put(PREVIEW_NAME, preview);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

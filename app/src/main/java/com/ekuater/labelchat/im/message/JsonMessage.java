
package com.ekuater.labelchat.im.message;

import com.ekuater.labelchat.im.PacketStruct;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * JSON type message
 * 
 * @author LinYong
 */
public class JsonMessage extends BaseMessage {

    private JSONObject mJson;

    public JsonMessage(int type) {
        this(type, new JSONObject());
        setCharset(PacketStruct.CHARSET_UTF_8);
    }

    public JsonMessage(int type, final JSONObject json) {
        super(type);
        mJson = json;
    }

    public JSONObject getJson() {
        return mJson;
    }

    public void setJson(final JSONObject json) {
        mJson = json;
    }

    @Override
    public byte[] toByteArray() {
        byte[] data = null;

        try {
            data = mJson.toString().getBytes(PacketStruct.CHARSET_UTF_8_NAME);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public void fromByteArray(byte[] content) {
        JSONObject json = null;

        try {
            json = new JSONObject(new String(content, PacketStruct.CHARSET_UTF_8_NAME));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mJson = (json != null) ? json : new JSONObject();
    }
}

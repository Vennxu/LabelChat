
package com.ekuater.labelchat.im.message;

import com.ekuater.labelchat.im.PacketStruct;

import java.io.UnsupportedEncodingException;

/**
 * StringMessage the content is String type
 * 
 * @author LinYong
 */
public class StringMessage extends BaseMessage {

    private String mContent;

    public StringMessage(int type) {
        super(type);
        setCharset(PacketStruct.CHARSET_UTF_8);
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    @Override
    public byte[] toByteArray() {
        String content = mContent;
        if (content == null) {
            content = "";
        }

        byte[] data = null;

        try {
            data = content.getBytes(PacketStruct.CHARSET_UTF_8_NAME);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public void fromByteArray(byte[] content) {
        if (content == null || content.length < 4) {
            return;
        }

        try {
            mContent = new String(content, PacketStruct.CHARSET_UTF_8_NAME);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}

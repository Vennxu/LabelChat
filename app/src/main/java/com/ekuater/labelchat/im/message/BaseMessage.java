
package com.ekuater.labelchat.im.message;

import com.ekuater.labelchat.im.PacketStruct;
import com.ekuater.labelchat.util.UUIDGenerator;

/**
 * Base class of message, convert to/from Packet.
 * 
 * @author LinYong
 */
public abstract class BaseMessage {

    private int mEncryptType;
    private int mType;
    private int mCharset;
    private long mTo;
    private long mFrom;

    private final String mUUID;

    public BaseMessage(int type) {
        mUUID = UUIDGenerator.generate();
        mEncryptType = PacketStruct.ENCRYPT_NONE;
        mType = type;
    }

    public String getUUID() {
        return mUUID;
    }

    public int getEncryptType() {
        return mEncryptType;
    }

    public void setEncryptType(int type) {
        mEncryptType = type;
    }

    public int getType() {
        return mType;
    }

    public int getCharset() {
        return mCharset;
    }

    public void setCharset(int charset) {
        mCharset = charset;
    }

    public long getTo() {
        return mTo;
    }

    public void setTo(long to) {
        mTo = to;
    }

    public long getFrom() {
        return mFrom;
    }

    public void setFrom(long from) {
        mFrom = from;
    }

    /**
     * Convert message content to Packet body
     * 
     * @return Packet body byte array
     */
    public abstract byte[] toByteArray();

    /**
     * Generate message body from Packet body
     * 
     * @param content content
     */
    public abstract void fromByteArray(byte[] content);
}

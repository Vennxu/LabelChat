
package com.ekuater.labelchat.im.message;

import com.ekuater.labelchat.im.PacketStruct;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Ping message send to server to hold the long connection between client and
 * server
 * 
 * @author LinYong
 */
public class PingMessage extends BaseMessage {

    private long mTimeMillis;

    public PingMessage() {
        super(PacketStruct.TYPE_PING);
        super.setCharset(PacketStruct.CHARSET_BINARY);
        setNow();
    }

    public long getTimeMillis() {
        return mTimeMillis;
    }

    public void setTimeMillis(long timeMillis) {
        mTimeMillis = timeMillis;
    }

    public void setNow() {
        mTimeMillis = System.currentTimeMillis();
    }

    @Override
    public void setCharset(int charset) {
        // Do nothing, PingMessage must be PacketStruct.CHARSET_BINARY
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        byte[] data = null;

        try {
            dataOut.writeLong(mTimeMillis);
            dataOut.flush();

            byteOut.flush();
            data = byteOut.toByteArray();

            dataOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public void fromByteArray(byte[] content) {
        if (content == null || content.length < 4) {
            return;
        }

        final DataInputStream dataIn = new DataInputStream(new ByteArrayInputStream(content));

        try {
            mTimeMillis = dataIn.readLong();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

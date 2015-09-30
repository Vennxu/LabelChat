
package com.ekuater.labelchat.im;

import com.ekuater.labelchat.im.message.BaseMessage;
import com.ekuater.labelchat.util.L;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author LinYong
 */
public class Packet {

    private static final String TAG = "Packet";

    private byte mProtocolVersion; // unsigned byte;
    private byte mEncryptType; // unsigned byte;
    private int mPipelineNumber; // unsigned integer;
    private byte mType; // unsigned byte;
    private byte mCharset; // unsigned byte;
    private long mSourceAddr; // unsigned integer
    private long mDestAddr; // unsigned integer
    private byte[] mBody;

    private Packet() {
    }

    public long getSourceAddress() {
        return mSourceAddr;
    }

    public long getDestAddress() {
        return mDestAddr;
    }

    public int getProtocolVersion() {
        return mProtocolVersion & 0xFF;
    }

    public int getEncryptType() {
        return mEncryptType & 0xFF;
    }

    public long getPipelineNumber() {
        return mPipelineNumber & 0xFFFFFFFFL;
    }

    public int getType() {
        return mType & 0xFF;
    }

    public int getCharset() {
        return mCharset & 0xFF;
    }

    public byte[] getBody() {
        return mBody;
    }

    /**
     * Convert packet to byte array
     * 
     * @return the byte array use to translate in socket output stream
     */
    public byte[] toByteArray() {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        byte[] data = null;

        try {
            long packetLen = PacketStruct.HEADER_TOTAL_LEN + mBody.length;
            dataOut.writeInt((int) packetLen);

            dataOut.writeByte(mProtocolVersion);
            dataOut.writeByte(mEncryptType);
            dataOut.writeInt(mPipelineNumber);
            dataOut.writeByte(mType);
            dataOut.writeByte(mCharset);
            dataOut.writeLong(mSourceAddr);
            dataOut.writeLong(mDestAddr);
            dataOut.write(mBody);
            dataOut.flush();

            byteOut.flush();
            data = byteOut.toByteArray();

            dataOut.close();
        } catch (IOException e) {
            L.e(TAG, "Packet to byte array error.");
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("from=" + mSourceAddr);
        sb.append(",to=" + mDestAddr);
        sb.append(",protocolVersion=" + mProtocolVersion);
        sb.append(",encryptType=" + mEncryptType);
        sb.append(",type=" + mType);
        sb.append(",charset=" + mCharset);
        sb.append(",body=" + printByteArray(mBody));

        return sb.toString();
    }

    private static String printByteArray(final byte[] array) {
        StringBuilder sb = new StringBuilder();

        sb.append("[ ");
        for (byte tmp : array) {
            sb.append(String.format("%02x", (tmp & 0xFF)));
            sb.append(" ");
        }
        sb.append("]");

        return sb.toString();
    }

    /**
     * Packet build class
     * 
     * @author LinYong
     */
    public static final class Builder {
        /**
         * Build Packet use packet header and body
         * 
         * @param header
         * @param body
         * @return
         */
        public static Packet build(final byte[] header, final byte[] body) {
            if ((header == null || header.length != PacketStruct.HEADER_TOTAL_LEN)
                    || (body == null || body.length <= 0)) {
                return null;
            }

            final DataInputStream dataIn = new DataInputStream(new ByteArrayInputStream(header));
            final Packet packet = new Packet();

            try {
                // skip the total length area
                dataIn.skip(PacketStruct.HEADER_PACKET_TOTAL_LEN_PART_LEN);

                packet.mProtocolVersion = dataIn.readByte();
                packet.mEncryptType = dataIn.readByte();
                packet.mPipelineNumber = dataIn.readInt();
                packet.mType = dataIn.readByte();
                packet.mCharset = dataIn.readByte();
                packet.mSourceAddr = dataIn.readLong();
                packet.mDestAddr = dataIn.readLong();
                packet.mBody = body;

                dataIn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return packet;
        }

        /**
         * Get Packet total length in socket input stream by packet header
         * 
         * @param header
         * @return
         */
        public static long getTotalLength(final byte[] header) {
            long length = 0;
            final DataInputStream dataIn = new DataInputStream(new ByteArrayInputStream(header));

            try {
                length = dataIn.readInt() & 0xFFFFFFFFL;
                dataIn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return length;
        }

        /**
         * Build Packet from message
         * 
         * @param message
         * @return
         */
        public static Packet build(final BaseMessage message) {
            final Packet packet = new Packet();

            packet.mProtocolVersion = PacketStruct.PROTOCOL_VERSION;
            packet.mPipelineNumber = 0;
            packet.mType = (byte) message.getType();
            packet.mCharset = (byte) message.getCharset();
            packet.mEncryptType = (byte) message.getEncryptType();
            packet.mSourceAddr = message.getFrom();
            packet.mDestAddr = message.getTo();
            packet.mBody = message.toByteArray();

            return packet;
        }
    }
}

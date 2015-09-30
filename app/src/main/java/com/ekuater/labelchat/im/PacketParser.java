
package com.ekuater.labelchat.im;

import com.ekuater.labelchat.im.IMException.PacketParserException;
import com.ekuater.labelchat.im.util.Streams;
import com.ekuater.labelchat.util.L;

import java.io.IOException;
import java.io.InputStream;

/**
 * Packet parser Parse the socket data to packet
 * 
 * @author LinYong
 */
public class PacketParser {

    private static final String TAG = "PacketParser";
    private static final int PACKET_HEADER_LEN = PacketStruct.HEADER_TOTAL_LEN;

    private InputStream mIn;

    public PacketParser() {
        ;
    }

    public void setInput(InputStream is) {
        mIn = is;
    }

    public Packet parseSinglePacket() throws IOException {
        final byte[] header = new byte[PACKET_HEADER_LEN];
        L.d(TAG, "parseSinglePacket(), reading header...");
        Streams.readFully(mIn, header);

        final long totalLen = Packet.Builder.getTotalLength(header);
        final long bodyLen = totalLen - PACKET_HEADER_LEN;
        final byte[] body = new byte[(int) bodyLen];
        L.d(TAG, "parseSinglePacket(), reading body, body length=" + bodyLen);
        Streams.readFully(mIn, body);
        L.d(TAG, "parseSinglePacket(), read body success");

        return Packet.Builder.build(header, body);
    }

    public Packet parse() throws PacketParserException {
        try {
            return parseSinglePacket();
        } catch (IOException e) {
            e.printStackTrace();
            throw new PacketParserException();
        }
    }
}

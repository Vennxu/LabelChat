
package com.ekuater.labelchat.im;

import com.ekuater.labelchat.util.L;

import java.io.InputStream;

/**
 * @author LinYong
 */
class PacketReader {

    private static final String TAG = "PacketReader";

    private Connection mConnection;
    private InputStream mIn;
    private Thread mReaderThread;
    private PacketParser mPacketParser;
    protected volatile boolean mDone;

    protected PacketReader(Connection connection) {
        mConnection = connection;
        init();
    }

    protected void init() {
        mIn = mConnection.getInputStream();
        mDone = false;
        mReaderThread = new Thread() {
            @Override
            public void run() {
                parsePackets(this);
            }
        };
        mReaderThread.setName("PacketReader");
        mReaderThread.setDaemon(true);
        resetParser();
    }

    public void startup() {
        L.d(TAG, "startup()");
        mReaderThread.start();
    }

    public void shutdown() {
        L.d(TAG, "shutdown()");
        mDone = true;
    }

    private void resetParser() {
        mPacketParser = new PacketParser();
        mPacketParser.setInput(mIn);
    }

    private void parsePackets(Thread thread) {
        try {
            while (!mDone && thread == mReaderThread) {
                L.d(TAG, "parsePackets(), start parse a packet");
                Packet packet = mPacketParser.parse();
                mConnection.processPacket(packet);
                L.d(TAG, "parsePackets(), packet=" + packet.toString());
            }
        } catch (Exception e) {
            if (!(mDone || mConnection.isSocketClosed())) {
                shutdown();
                mConnection.notifyConnectionError(e);
            }
        }
    }
}

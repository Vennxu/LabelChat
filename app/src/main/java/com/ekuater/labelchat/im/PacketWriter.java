
package com.ekuater.labelchat.im;

import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.im.IMException.NotConnectedException;
import com.ekuater.labelchat.im.util.ArrayBlockingQueueWithShutdown;
import com.ekuater.labelchat.util.L;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class write packet in a new thread
 * 
 * @author LinYong
 */
class PacketWriter {

    private static final String TAG = "PacketWriter";
    private static final int QUEUE_SIZE = 500;

    private final Connection mConnection;
    private final ArrayBlockingQueueWithShutdown<Packet> mQueue = new ArrayBlockingQueueWithShutdown<Packet>(
            QUEUE_SIZE, true);

    private Thread mWriterThread;
    private OutputStream mOut;
    protected volatile boolean mDone;
    private final AtomicBoolean mShutdownDone = new AtomicBoolean(false);
    private Packet mWritingPacket = null;

    protected PacketWriter(Connection connection) {
        mConnection = connection;
        init();
    }

    protected void init() {
        mOut = mConnection.getOutputStream();
        mDone = false;
        mShutdownDone.set(false);
        mQueue.start();
        mWriterThread = new Thread() {
            @Override
            public void run() {
                writePackets(this);
            }
        };
        mWriterThread.setName("");
        mWriterThread.setDaemon(true);
    }

    void setOutputStream(OutputStream out) {
        mOut = out;
    }

    public void sendPacket(Packet packet) throws NotConnectedException {
        if (mDone) {
            throw new NotConnectedException();
        }

        try {
            mQueue.put(packet);
        } catch (InterruptedException ie) {
            throw new NotConnectedException();
        }
    }

    public void startup() {
        L.d(TAG, "startup()");
        mWriterThread.start();
    }

    public void shutdown() {
        L.d(TAG, "shutdown()");
        mDone = true;
        mQueue.shutdown();
        synchronized (mShutdownDone) {
            if (!mShutdownDone.get()) {
                try {
                    mShutdownDone.wait(mConnection.getPacketReplyTimeout());
                } catch (InterruptedException e) {
                    L.e(TAG, "shutdown", e);
                }
            }
        }
    }

    private Packet nextPacket() {
        if (mDone) {
            return null;
        }

        Packet packet = null;
        try {
            packet = mQueue.take();
        } catch (InterruptedException e) {
            // Do nothing
        }
        return packet;
    }

    private void notifyPacketWritten(int result) {
        if (mWritingPacket != null) {
            mConnection.notifyPacketWritten(mWritingPacket, result);
            mWritingPacket = null;
        }
    }

    private void writePackets(Thread thisThread) {
        try {
            while (!mDone && (mWriterThread == thisThread)) {
                Packet packet = nextPacket();
                if (packet != null) {
                    mWritingPacket = packet;
                    writePacket(packet);
                    notifyPacketWritten(ConstantCode.SEND_RESULT_SUCCESS);

                    if (mQueue.isEmpty()) {
                        mOut.flush();
                    }
                }
            }

            try {
                while (!mQueue.isEmpty()) {
                    Packet packet = mQueue.remove();
                    mWritingPacket = packet;
                    writePacket(packet);
                    notifyPacketWritten(ConstantCode.SEND_RESULT_SUCCESS);
                }
                mOut.flush();
            } catch (Exception e) {
                L.e(TAG, "Exception flushing queue during shutdown, ignore and continue", e);
                notifyPacketWritten(ConstantCode.SEND_RESULT_NETWORK_ERROR);
            }

            while (!mQueue.isEmpty()) {
                mWritingPacket = mQueue.remove();
                notifyPacketWritten(ConstantCode.SEND_RESULT_CONNECTION_CLOSE);
            }
            mQueue.clear();
            mConnection.clearSendingMessage();

            try {
                mOut.close();
            } catch (Exception e) {
                // Do nothing
            }

            mShutdownDone.set(true);
            synchronized (mShutdownDone) {
                mShutdownDone.notify();
            }
        } catch (IOException e) {
            if (!(mDone || mConnection.isSocketClosed())) {
                shutdown();
                mConnection.notifyConnectionError(e);
            }
            notifyPacketWritten(ConstantCode.SEND_RESULT_NETWORK_ERROR);
        }
    }

    private void writePacket(Packet packet) throws IOException {
        if (packet == null) {
            return;
        }

        byte[] data = packet.toByteArray();

        if (data == null || data.length <= 0) {
            return;
        }

        mOut.write(data);
        mOut.flush();

        L.d(TAG, "writePacket(),byte[]:" + printByteArray(data));
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
}

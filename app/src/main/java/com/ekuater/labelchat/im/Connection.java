
package com.ekuater.labelchat.im;

import android.text.TextUtils;

import com.ekuater.labelchat.im.IMException.NotConnectedException;
import com.ekuater.labelchat.im.message.AuthenticationMessage;
import com.ekuater.labelchat.im.message.BaseMessage;
import com.ekuater.labelchat.util.L;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author LinYong
 */
public class Connection {

    private static final String TAG = "Connection";

    /**
     * Counter to uniquely identify connections that are created.
     */
    private static final AtomicInteger sConnectionCounter = new AtomicInteger(0);

    /**
     * A set of listeners which will be invoked if a new connection is created.
     */
    private static final Set<ConnectionCreationListener> sConnectionEstablishedListeners =
            new CopyOnWriteArraySet<ConnectionCreationListener>();

    private static final class ExecutorThreadFactory implements ThreadFactory {

        private final int mConnectionCounterValue;
        private int mCount = 0;

        private ExecutorThreadFactory(int connectionCounterValue) {
            this.mConnectionCounterValue = connectionCounterValue;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "Connection Executor Service " + mCount++ + " ("
                    + mConnectionCounterValue + ")");
            thread.setDaemon(true);
            return thread;
        }

    }

    /**
     * Adds a new listener that will be notified when new Connections are
     * created. Note that newly created connections will not be actually
     * connected to the server.
     * 
     * @param connectionCreationListener a listener interested on new
     *            connections.
     */
    public static void addConnectionCreationListener(
            ConnectionCreationListener connectionCreationListener) {
        sConnectionEstablishedListeners.add(connectionCreationListener);
    }

    /**
     * Removes a listener that was interested in connection creation events.
     * 
     * @param connectionCreationListener a listener interested on new
     *            connections.
     */
    public static void removeConnectionCreationListener(
            ConnectionCreationListener connectionCreationListener) {
        sConnectionEstablishedListeners.remove(connectionCreationListener);
    }

    /**
     * Get the collection of listeners that are interested in connection
     * creation events.
     * 
     * @return a collection of listeners interested on new connections.
     */
    public static Collection<ConnectionCreationListener> getConnectionCreationListeners() {
        return Collections.unmodifiableCollection(sConnectionEstablishedListeners);
    }

    private final int mConnectionCounterValue = sConnectionCounter.getAndIncrement();
    private final ScheduledExecutorService mExecutorService = new ScheduledThreadPoolExecutor(2,
            new ExecutorThreadFactory(mConnectionCounterValue));

    private final List<IMessageListener> mRecvListeners = new CopyOnWriteArrayList<IMessageListener>();
    private final List<IConnectionListener> mConnectionListeners = new CopyOnWriteArrayList<IConnectionListener>();

    private long mPacketReplyTimeout = ConnectionConfiguration.getDefaultPacketReplyTimeout();
    private ConnectionConfiguration mConfig;
    private Socket mSocket;
    private volatile boolean mSocketClosed = false;
    private boolean mConnected = false;
    private InputStream mIn;
    private OutputStream mOut;

    private PacketReader mPacketReader;
    private PacketWriter mPacketWriter;

    // for auto reconnect
    private long mUserID;
    private String mAuth;
    private boolean mAuthenticated;
    private boolean mWasAuthenticated;

    private final Map<Packet, String> mSendingMessageMap = new ConcurrentHashMap<Packet, String>();

    public Connection(ConnectionConfiguration config) {
        mConfig = config;
    }

    /**
     * Returns the configuration used to connect to the server.
     * 
     * @return the configuration used to connect to the server.
     */
    public ConnectionConfiguration getConfiguration() {
        return mConfig;
    }

    public void connect() throws IOException {
        if (isConnected()) {
            return;
        }

        connectInternal();
    }

    public void disconnect() {
        if (!isConnected()) {
            return;
        }

        shutdown();
        callConnectionClosedListener();
    }

    /**
     * Authenticate the connection by sending authentication information to
     * server.
     * 
     * @param auth Authentication information
     * @throws NotConnectedException
     */
    public void authenticate(long userID, String auth) throws NotConnectedException {
        AuthenticationMessage authMessage = new AuthenticationMessage();

        authMessage.setTo(0L);
        authMessage.setFrom(userID);
        authMessage.setContent(auth);

        sendMessage(authMessage);

        mUserID = userID;
        mAuth = auth;
        mAuthenticated = true;
        callAuthenticatedListener();
    }

    public boolean isConnected() {
        return mConnected;
    }

    public boolean isSocketClosed() {
        return mSocketClosed;
    }

    public boolean isAuthenticated() {
        return mAuthenticated;
    }

    public long getPacketReplyTimeout() {
        return mPacketReplyTimeout;
    }

    public void setPacketReplyTimeout(long timeout) {
        mPacketReplyTimeout = timeout;
    }

    protected void sendPacket(Packet packet) throws NotConnectedException {
        sendPacketInternal(packet);
    }

    public void sendMessage(BaseMessage message) throws NotConnectedException {
        Packet packet = Packet.Builder.build(message);
        sendPacketInternal(packet);
        mSendingMessageMap.put(packet, message.getUUID());
    }

    /**
     * Adds a connection listener to this connection that will be notified when
     * the connection closes or fails.
     * 
     * @param connectionListener a connection listener.
     */
    public void addConnectionListener(IConnectionListener connectionListener) {
        if (connectionListener == null) {
            return;
        }
        if (!mConnectionListeners.contains(connectionListener)) {
            mConnectionListeners.add(connectionListener);
        }
    }

    /**
     * Removes a connection listener from this connection.
     * 
     * @param connectionListener a connection listener.
     */
    public void removeConnectionListener(IConnectionListener connectionListener) {
        mConnectionListeners.remove(connectionListener);
    }

    /**
     * Registers a message listener with this connection. A message listener
     * will be invoked only when an incoming message is received.
     * 
     * @param listener the packet listener to notify of new received packets.
     */
    public void addMessageListener(IMessageListener listener) {
        if (listener == null) {
            return;
        }
        if (!mRecvListeners.contains(listener)) {
            mRecvListeners.add(listener);
        }
    }

    /**
     * Removes a message listener for received messages from this connection.
     * 
     * @param listener the packet listener to remove.
     */
    public void removeMessageListener(IMessageListener listener) {
        mRecvListeners.remove(listener);
    }

    private void sendPacketInternal(Packet packet) throws NotConnectedException {
        mPacketWriter.sendPacket(packet);
    }

    public InputStream getInputStream() {
        return mIn;
    }

    public OutputStream getOutputStream() {
        return mOut;
    }

    private void connectInternal() throws IOException {
        boolean firstInit = (mPacketReader == null) || (mPacketWriter == null);

        try {
            createSocket();
            initScoketStream();
            initPacketReaderWriter(firstInit);
            mConnected = true;
            callConnectionConnectedListener();

            if (firstInit) {
                for (ConnectionCreationListener listener : getConnectionCreationListeners()) {
                    listener.connectionCreated(this);
                }
            }
        } catch (IOException e) {
            shutdown();
            throw e;
        }
    }

    private void shutdown() {
        if (mPacketReader != null) {
            mPacketReader.shutdown();
        }
        if (mPacketWriter != null) {
            mPacketWriter.shutdown();
        }
        try {
            if (mSocket != null) {
                mSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mAuthenticated = false;
        mSocketClosed = true;
        mConnected = false;
        mIn = null;
        mOut = null;
    }

    private void createSocket() throws IOException {
        HostAddress hostAddr = mConfig.getHostAddress();
        InetAddress inetAddr = InetAddress.getByName(hostAddr.getAddress());

        mSocket = new Socket(inetAddr, hostAddr.getPort());
        mSocketClosed = false;
    }

    private void initScoketStream() throws IOException {
        mIn = mSocket.getInputStream();
        mOut = mSocket.getOutputStream();
    }

    private void initPacketReaderWriter(boolean firstInit) {
        if (firstInit) {
            mPacketReader = new PacketReader(this);
            mPacketWriter = new PacketWriter(this);
        } else {
            mPacketReader.init();
            mPacketWriter.init();
        }

        mPacketReader.startup();
        mPacketWriter.startup();
    }

    synchronized void notifyConnectionError(Exception e) {
        // Listeners were already notified of the exception, return right here.
        if ((mPacketReader == null || mPacketReader.mDone) &&
                (mPacketWriter == null || mPacketWriter.mDone)) {
            return;
        }

        // Closes the connection temporary. A reconnection is possible
        shutdown();

        // Notify connection listeners of the error.
        callConnectionClosedOnErrorListener(e);
    }

    private void callConnectionClosedOnErrorListener(Exception e) {
        L.d(TAG, "Connection closed with error", e);

        for (IConnectionListener listener : mConnectionListeners) {
            try {
                listener.connectionClosedOnError(e);
            } catch (Exception e2) {
                // Catch and print any exception so we can recover
                // from a faulty listener
                L.d(TAG, "Error in listener while closing connection", e2);
            }
        }
    }

    private void callConnectionConnectedListener() {
        for (IConnectionListener listener : mConnectionListeners) {
            try {
                listener.connected(this);
            } catch (Exception e) {
                // Catch and print any exception so we can recover
                // from a faulty listener
                L.d(TAG, "Error in listener while connection connected", e);
            }
        }
    }

    private void callConnectionClosedListener() {
        for (IConnectionListener listener : mConnectionListeners) {
            try {
                listener.connectionClosed();
            } catch (Exception e) {
                // Catch and print any exception so we can recover
                // from a faulty listener and finish the shutdown process
                L.d(TAG, "Error in listener while closing connection", e);
            }
        }
    }

    private void callAuthenticatedListener() {
        final boolean success = isAuthenticated();

        for (IConnectionListener listener : mConnectionListeners) {
            try {
                listener.authenticateResult(this, success);
            } catch (Exception e) {
                // Catch and print any exception so we can recover
                // from a faulty listener and finish the shutdown process
                L.d(TAG, "Error in listener while connection authenticated", e);
            }
        }
    }

    void processPacket(Packet packet) {
        if (packet == null) {
            return;
        }

        // Deliver the incoming packet to listeners.
        mExecutorService.submit(new NewPacketNotification(packet));
    }

    /**
     * A runnable to notify all listeners of a packet.
     */
    private class NewPacketNotification implements Runnable {

        private final Packet mPacket;

        public NewPacketNotification(Packet packet) {
            mPacket = packet;
        }

        @Override
        public void run() {
            if (mPacket == null) {
                return;
            }

            BaseMessage message = PacketUtil.toMessage(mPacket);
            if (message == null) {
                return;
            }

            switch (message.getType()) {
                case PacketStruct.TYPE_AUTHENTICATION:
                    break;

                default:
                    for (IMessageListener listener : mRecvListeners) {
                        try {
                            if (listener != null) {
                                listener.processMessage(message);
                            }
                        } catch (NotConnectedException e) {
                            L.d(TAG, "Got not connected exception, aborting", e);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

    /**
     * A runnable to notify message sending result.
     */
    private class PacketWrittenNotification implements Runnable {

        private final Packet mPacket;
        private final int mResult;

        public PacketWrittenNotification(Packet packet, int result) {
            mPacket = packet;
            mResult = result;
        }

        @Override
        public void run() {
            if (mPacket == null) {
                return;
            }

            String msgUUID = getMessageUUID(mPacket);
            if (TextUtils.isEmpty(msgUUID)) {
                return;
            }

            switch (mPacket.getType()) {
                case PacketStruct.TYPE_AUTHENTICATION:
                    // TODO
                    break;

                default:
                    for (IMessageListener listener : mRecvListeners) {
                        try {
                            if (listener != null) {
                                listener.onMessageWrittenResult(msgUUID, mResult);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }

        private String getMessageUUID(Packet packet) {
            return mSendingMessageMap.remove(packet);
        }
    }

    void notifyPacketWritten(Packet packet, int result) {
        if (packet == null) {
            return;
        }

        // Deliver the incoming packet to listeners.
        mExecutorService.submit(new PacketWrittenNotification(packet, result));
    }

    /* package */List<IConnectionListener> getConnectionListeners() {
        return mConnectionListeners;
    }

    void clearSendingMessage() {
        mSendingMessageMap.clear();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            mExecutorService.shutdownNow();
        } finally {
            super.finalize();
        }
    }
}

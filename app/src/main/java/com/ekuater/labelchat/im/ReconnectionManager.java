
package com.ekuater.labelchat.im;

public class ReconnectionManager extends AbstractConnectionListener {

    private static final int RECONNECT_DELAY = 10; // seconds

    static {
        Connection.addConnectionCreationListener(new ConnectionCreationListener() {
            @Override
            public void connectionCreated(Connection connection) {
                connection.addConnectionListener(new ReconnectionManager(connection));
            }
        });
    }

    private Connection mConnection;
    private Thread mReconnectionThread;
    // Holds the state of the reconnection
    private boolean mDone = false;

    private ReconnectionManager(Connection connection) {
        mConnection = connection;
    }

    /**
     * Returns true if the reconnection mechanism is enabled.
     * 
     * @return true if automatic re-connections are allowed.
     */
    private boolean isReconnectionAllowed() {
        return !mDone && !mConnection.isConnected()
                && mConnection.getConfiguration().isReconnectionAllowed();
    }

    synchronized private void reconnect() {
        if (!isReconnectionAllowed()) {
            return;
        }

        // Since there is no thread running, creates a new one to attempt the
        // reconnection.
        if (mReconnectionThread != null && mReconnectionThread.isAlive()) {
            return;
        }

        mReconnectionThread = new Thread() {

            @Override
            public void run() {
                while (isReconnectionAllowed()) {
                    int remainingSeconds = RECONNECT_DELAY;
                    while (isReconnectionAllowed() && remainingSeconds > 0) {
                        try {
                            Thread.sleep(1000);
                            remainingSeconds--;
                            notifyAttemptToReconnectIn(remainingSeconds);
                        } catch (InterruptedException e) {
                            notifyReconnectionFailed(e);
                        }
                    }

                    try {
                        if (isReconnectionAllowed()) {
                            mConnection.connect();
                        }
                    } catch (Exception e) {
                        notifyReconnectionFailed(e);
                    }
                }
            }
        };
        mReconnectionThread.setName("Reconnection Manager");
        mReconnectionThread.setDaemon(true);
        mReconnectionThread.start();
    }

    private void notifyReconnectionFailed(Exception exception) {
        if (isReconnectionAllowed()) {
            for (IConnectionListener listener : mConnection.getConnectionListeners()) {
                listener.reconnectionFailed(exception);
            }
        }
    }

    private void notifyAttemptToReconnectIn(int seconds) {
        if (isReconnectionAllowed()) {
            for (IConnectionListener listener : mConnection.getConnectionListeners()) {
                listener.reconnectingIn(seconds);
            }
        }
    }

    @Override
    public void connectionClosed() {
        mDone = true;
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        mDone = false;
        if (isReconnectionAllowed()) {
            reconnect();
        }
    }
}


package com.ekuater.labelchat.im;

/**
 * Configuration of connection
 * 
 * @author LinYong
 */
public class ConnectionConfiguration {

    private static final long DEFAULT_PACKET_REPLY_TIMEOUT = 5000L;

    private HostAddress mHostAddress;
    private boolean mReconnectionAllowed = false;

    public static long getDefaultPacketReplyTimeout() {
        return DEFAULT_PACKET_REPLY_TIMEOUT;
    }

    public ConnectionConfiguration() {
        ;
    }

    public HostAddress getHostAddress() {
        return mHostAddress;
    }

    public void setHostAddress(HostAddress host) {
        mHostAddress = host;
    }

    public void setHostAddress(String host, int port) {
        mHostAddress = new HostAddress(host, port);
    }

    /**
     * Sets if the reconnection mechanism is allowed to be used. By default
     * reconnection is allowed.
     * 
     * @param isAllowed if the reconnection mechanism should be enabled for this
     *            connection.
     */
    public void setReconnectionAllowed(boolean isAllowed) {
        mReconnectionAllowed = isAllowed;
    }

    /**
     * Returns if the reconnection mechanism is allowed to be used. By default
     * reconnection is allowed. You can disable the reconnection mechanism with
     * {@link #setReconnectionAllowed(boolean)}.
     * 
     * @return true, if the reconnection mechanism is enabled.
     */
    public boolean isReconnectionAllowed() {
        return mReconnectionAllowed;
    }
}


package com.ekuater.labelchat.im;

/**
 * host address
 * 
 * @author LinYong
 */
public class HostAddress {

    private String mAddr;
    private int mPort;

    public HostAddress(String host, int port) {
        mAddr = host;
        mPort = port;
    }

    public String getAddress() {
        return mAddr;
    }

    public int getPort() {
        return mPort;
    }
}

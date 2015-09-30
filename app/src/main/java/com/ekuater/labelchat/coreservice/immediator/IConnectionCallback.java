
package com.ekuater.labelchat.coreservice.immediator;

/**
 * @author LinYong
 */
public interface IConnectionCallback {

    public void needReconnect();

    public boolean isConnectionConnected();
}

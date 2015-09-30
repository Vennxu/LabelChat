
package com.ekuater.labelchat.im;

/**
 * The AbstractConnectionListener class provides an empty implementation for all
 * methods defined by the {@link IConnectionListener} interface. This is a
 * convenience class which should be used in case you do not need to implement
 * all methods.
 * 
 * @author LinYong
 */
public class AbstractConnectionListener implements IConnectionListener {

    @Override
    public void connected(Connection connection) {
        // do nothing
    }

    @Override
    public void authenticateResult(Connection connection, boolean success) {
        // do nothing
    }

    @Override
    public void connectionClosed() {
        // do nothing
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        // do nothing
    }

    @Override
    public void reconnectingIn(int seconds) {
        // do nothing
    }

    @Override
    public void reconnectionSuccessful() {
        // do nothing
    }

    @Override
    public void reconnectionFailed(Exception e) {
        // do nothing
    }
}

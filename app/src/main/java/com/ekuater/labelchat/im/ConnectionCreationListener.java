
package com.ekuater.labelchat.im;

/**
 * @author LinYong
 */
public interface ConnectionCreationListener {

    /**
     * Notification that a new connection has been created. The new connection
     * will not yet be connected to the server.
     * 
     * @param connection the newly created connection.
     */
    public void connectionCreated(Connection connection);
}

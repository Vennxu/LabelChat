
package com.ekuater.labelchat.im;

import com.ekuater.labelchat.im.IMException.NotConnectedException;
import com.ekuater.labelchat.im.message.BaseMessage;

/**
 * @author LinYong
 */
public interface IMessageListener {

    /**
     * Process the next message sent to this packet listener. A single thread is
     * responsible for invoking all listeners, so it's very important that
     * implementations of this method not block for any extended period of time.
     * 
     * @param message the message to process.
     * @throws NotConnectedException
     */
    public void processMessage(BaseMessage message) throws NotConnectedException;

    /**
     * Notify message written result, success or failed.
     * 
     * @param messageUUID the UUID of message written
     * @param result message written result, success or error code
     */
    public void onMessageWrittenResult(String messageUUID, int result);
}

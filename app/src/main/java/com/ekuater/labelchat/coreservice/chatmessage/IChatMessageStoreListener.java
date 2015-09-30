
package com.ekuater.labelchat.coreservice.chatmessage;

import com.ekuater.labelchat.datastruct.ChatMessage;

/**
 * @author LinYong
 */
public interface IChatMessageStoreListener {

    /**
     * notify new chat message has been received.
     *
     * @param chatMsg
     */
    public void onNewMessageReceived(ChatMessage chatMsg);

    /**
     * notify the mew ChatMessage is now sending.
     *
     * @param messageSession unique String passed by client to identify
     *                       ChatMessage.
     * @param messageId      new ChatMessage ID generated while sending.
     */
    public void onNewMessageSending(String messageSession, long messageId);

    /**
     * notify the message send result
     *
     * @param messageSession unique String passed by client to identify
     *                       ChatMessage.
     * @param messageId      chat message id
     * @param result         send success or error code
     */
    public void onMessageSendResult(String messageSession, long messageId, int result);
}

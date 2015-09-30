
package com.ekuater.labelchat.coreservice.immediator;

import com.ekuater.labelchat.datastruct.ChatMessage;

/**
 * @author LinYong
 */
public interface IIMListener {

    public void onConnectResult(int result);

    public void onChatMessageSendResult(ChatMessage chatMessage, int result);

    public void onNewChatMessageReceived(ChatMessage chatMessage);

    public void onJoinLabelChatRoomResult(String labelId, int result);

    public void onQuitLabelChatRoomResult(String labelId, int result);

    public void onJoinNormalChatRoomResult(String chatRoomId, int result);

    public void onQuitNormalChatRoomResult(String chatRoomId, int result);
}

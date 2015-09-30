package com.ekuater.labelchat.coreservice.event;

/**
 * Created by Leo on 2015/1/30.
 * Received chat message sent user query event
 *
 * @author LinYong
 */
public class ChatUserEvent {

    public static enum ChatType {
        PRIVATE,
        GROUP,
        LABEL_CHAT_ROOM,
        NORMAL_CHAT_ROOM,
    }

    private final String userId;
    private final ChatType chatType;
    private ChatUserGotEvent syncGotEvent;

    public ChatUserEvent(String userId, ChatType chatType) {
        this.userId = userId;
        this.chatType = chatType;
        this.syncGotEvent = null;
    }

    public String getUserId() {
        return userId;
    }

    public ChatType getChatType() {
        return chatType;
    }

    public ChatUserGotEvent getSyncGotEvent() {
        return syncGotEvent;
    }

    public void setSyncGotEvent(ChatUserGotEvent syncGotEvent) {
        this.syncGotEvent = syncGotEvent;
    }
}

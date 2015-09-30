package com.ekuater.labelchat.coreservice.event;

/**
 * Created by Leo on 2015/1/30.
 *
 * @author LinYong
 */
public class ChatUserGotEvent {

    public static enum UserType {
        CONTACT,
        STRANGER,
        LITE_STRANGER,
    }

    private final String userId;
    private final UserType userType;

    public ChatUserGotEvent(String userId, UserType userType) {
        this.userId = userId;
        this.userType = userType;
    }

    public String getUserId() {
        return userId;
    }

    public UserType getUserType() {
        return userType;
    }
}

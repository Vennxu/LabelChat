package com.ekuater.labelchat.delegate.event;

/**
 * Created by Leo on 2015/4/23.
 *
 * @author LinYong
 */
public class LoginEvent {

    public enum From {
        ACCOUNT_MANAGER, FILE_UPLOADER
    }

    private final int result;
    private final From from;

    public LoginEvent(int result, From from) {
        this.result = result;
        this.from = from;
    }

    public int getResult() {
        return result;
    }

    public From getFrom() {
        return from;
    }
}

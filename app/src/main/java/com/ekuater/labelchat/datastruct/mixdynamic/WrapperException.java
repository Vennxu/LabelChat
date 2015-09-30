package com.ekuater.labelchat.datastruct.mixdynamic;

/**
 * Created by Leo on 2015/4/18.
 *
 * @author LinYong
 */
public class WrapperException extends Exception {

    private static final long serialVersionUID = 642296511450536080L;

    public WrapperException() {
        super();
    }

    public WrapperException(String detailMessage) {
        super(detailMessage);
    }

    public WrapperException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public WrapperException(Throwable throwable) {
        super(throwable);
    }
}

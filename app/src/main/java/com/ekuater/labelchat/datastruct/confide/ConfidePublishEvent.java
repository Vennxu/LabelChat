package com.ekuater.labelchat.datastruct.confide;

import com.ekuater.labelchat.datastruct.Confide;

/**
 * Created by Leo on 2015/4/10.
 *
 * @author LinYong
 */
public class ConfidePublishEvent {

    private final Confide confide;

    public ConfidePublishEvent(Confide confide) {
        this.confide = confide;
    }

    public Confide getConfide() {
        return confide;
    }
}

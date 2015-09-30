package com.ekuater.labelchat.coreservice.event;

import com.ekuater.labelchat.datastruct.SystemPush;

/**
 * Created by Leo on 2015/3/30.
 *
 * @author LinYong
 */
public class NewSystemPushEvent {

    private final SystemPush systemPush;

    public NewSystemPushEvent(SystemPush systemPush) {
        this.systemPush = systemPush;
    }

    public SystemPush getSystemPush() {
        return systemPush;
    }
}

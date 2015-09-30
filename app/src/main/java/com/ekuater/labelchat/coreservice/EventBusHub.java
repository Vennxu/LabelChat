package com.ekuater.labelchat.coreservice;

import de.greenrobot.event.EventBus;

/**
 * Created by Leo on 2015/1/30.
 *
 * @author LinYong
 */
public class EventBusHub {

    private static final EventBus sCoreEventBus = new EventBus();
    private static final EventBus sChatEventBus = new EventBus();

    public static EventBus getCoreEventBus() {
        return sCoreEventBus;
    }

    public static EventBus getChatEventBus() {
        return sChatEventBus;
    }
}

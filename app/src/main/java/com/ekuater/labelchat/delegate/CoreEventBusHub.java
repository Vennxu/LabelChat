package com.ekuater.labelchat.delegate;

import de.greenrobot.event.EventBus;

/**
 * Created by Leo on 2015/4/23.
 *
 * @author LinYong
 */
public class CoreEventBusHub {

    private static final EventBus sDefaultEventBus = new EventBus();

    public static EventBus getDefaultEventBus() {
        return sDefaultEventBus;
    }
}

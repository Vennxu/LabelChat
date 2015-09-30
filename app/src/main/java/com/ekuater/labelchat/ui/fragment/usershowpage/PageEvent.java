package com.ekuater.labelchat.ui.fragment.usershowpage;

/**
 * Created by Leo on 2015/2/3.
 *
 * @author LinYong
 */
public class PageEvent {

    public static enum Event {
        LOAD_DONE,
        CONTACT_UPDATE,
    }

    public BasePage page;
    public Event event;
    public Object extra;

    public PageEvent(BasePage page, Event event) {
        this(page, event, null);
    }

    public PageEvent(BasePage page, Event event, Object extra) {
        this.page = page;
        this.event = event;
        this.extra = extra;
    }
}

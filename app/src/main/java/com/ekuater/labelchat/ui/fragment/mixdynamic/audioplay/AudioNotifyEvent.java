package com.ekuater.labelchat.ui.fragment.mixdynamic.audioplay;

/**
 * Created by Leo on 2015/4/25.
 *
 * @author LinYong
 */
public class AudioNotifyEvent {

    public enum NotifyType {
        STATE_NOTIFY, TIME_NOTIFY
    }

    private final NotifyType type;
    private final AudioEntity entity;

    public AudioNotifyEvent(NotifyType type, AudioEntity entity) {
        this.type = type;
        this.entity = entity;
    }

    public NotifyType getType() {
        return type;
    }

    public AudioEntity getEntity() {
        return entity;
    }
}

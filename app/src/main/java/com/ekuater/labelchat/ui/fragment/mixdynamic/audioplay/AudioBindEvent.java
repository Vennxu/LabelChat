package com.ekuater.labelchat.ui.fragment.mixdynamic.audioplay;

/**
 * Created by Leo on 2015/4/25.
 *
 * @author LinYong
 */
public class AudioBindEvent {

    private final String id;
    private AudioEntity entity;

    public AudioBindEvent(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public AudioEntity getEntity() {
        return entity;
    }

    public void setEntity(AudioEntity entity) {
        this.entity = entity;
    }
}

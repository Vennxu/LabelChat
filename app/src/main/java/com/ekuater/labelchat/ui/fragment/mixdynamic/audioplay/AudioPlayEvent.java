package com.ekuater.labelchat.ui.fragment.mixdynamic.audioplay;

/**
 * Created by Leo on 2015/4/25.
 *
 * @author LinYong
 */
public class AudioPlayEvent {

    private final AudioEntity entity;

    public AudioPlayEvent(AudioEntity entity) {
        this.entity = entity;
    }

    public AudioEntity getEntity() {
        return entity;
    }
}

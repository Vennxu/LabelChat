package com.ekuater.labelchat.ui.fragment.mixdynamic.audioplay;

/**
 * Created by Leo on 2015/4/25.
 *
 * @author LinYong
 */
public class AudioEntity {

    private final String id;
    private final String media;
    private AudioState state;
    private String time;
    private String type;

    public AudioEntity(String id, String media, String type) {
        this.id = id;
        this.media = media;
        this.state = AudioState.STOPPED;
        this.time = null;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getMedia() {
        return media;
    }

    public AudioState getState() {
        return state;
    }

    public void setState(AudioState state) {
        this.state = state;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }


}

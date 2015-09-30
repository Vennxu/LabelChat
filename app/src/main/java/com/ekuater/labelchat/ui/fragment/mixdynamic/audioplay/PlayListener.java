package com.ekuater.labelchat.ui.fragment.mixdynamic.audioplay;

/**
 * Created by Leo on 2015/4/25.
 *
 * @author LinYong
 */
public interface PlayListener {

    public void onPrepared();

    public void onCompletion();

    public void onTimeChanged(String time);
}

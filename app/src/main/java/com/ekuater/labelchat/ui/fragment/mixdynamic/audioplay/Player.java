package com.ekuater.labelchat.ui.fragment.mixdynamic.audioplay;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Leo on 2015/4/25.
 *
 * @author LinYong
 */
public class Player implements MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private static final int PERIOD = 500;
    /**
     * 线程池。
     */
    private static ExecutorService SERVICE = Executors.newFixedThreadPool(5);

    private MediaPlayer mediaPlayer;
    private PlayListener listener;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer == null) {
                return;
            }
            if (mediaPlayer.isPlaying()) {
                updatePlayTime();
            }
            handler.postDelayed(this, PERIOD);
        }
    };

    public Player() {
    }

    /**
     * 创建MediaPlayer
     */
    private void createMediaPlayer() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
        } catch (Exception e) {
            e.printStackTrace();
            stop();
            onCompletion(mediaPlayer);
            release();
        }
    }

    /**
     * 计时
     */
    private void timing() {
        handler.postDelayed(runnable, 0);
    }

    public void updatePlayTime() {
        int position = mediaPlayer.getCurrentPosition();
        int duration = mediaPlayer.getDuration();
        if (listener != null) {
            listener.onTimeChanged(getDuration(position) + "/" + getDuration(duration));
        }
    }

    public void setPlayListener(PlayListener listener) {
        this.listener = listener;
    }

    /**
     * 新开一个线程，播放音频。
     *
     * @param url media path
     */
    public void playUrl(final String url) {
        stop();
        if (mediaPlayer == null) {
            createMediaPlayer();
        }
        timing();
        SERVICE.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(url);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void stop() {
        handler.removeCallbacks(runnable);
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * 通过onPrepared播放
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            if (listener != null) {
                listener.onPrepared();
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        handler.removeCallbacks(runnable);
        if (mediaPlayer != null) {
            stop();
            if (listener != null) {
                listener.onCompletion();
            }
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer arg0, int bufferingProgress) {
    }

    private String getDuration(long duration) {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        return String.format(Locale.ENGLISH, "%1$02d:%2$02d", minutes % 60, seconds % 60);
    }
}

package com.ekuater.labelchat.ui.fragment.voice;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Handler;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 播放网络音频。
 */
public class Player implements OnBufferingUpdateListener, OnCompletionListener,
        MediaPlayer.OnPreparedListener {

    public MediaPlayer mediaPlayer;
    private MyPlayerCallback callback;
    private static final int PERIOD = 500;
    public int currentPosition = 0;
    private TextView voiceTime;

    /**
     * 线程池。
     */
    private ExecutorService service = Executors.newFixedThreadPool(10);

    public Player(TextView voiceTime) {
        this.voiceTime = voiceTime;
        createMediaPlayer();
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
        }
    }

    Runnable runnable;
    final Handler handler = new Handler();

    /**
     * 计时
     */
    private void timing() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer == null) {
                    return;
                }
                if (mediaPlayer.isPlaying()) {
                    setSeekBarProgress();
                }
                handler.postDelayed(this, PERIOD);
            }
        };
        handler.postDelayed(runnable, 0);
    }

    public void setSeekBarProgress() {
        int position = mediaPlayer.getCurrentPosition();
        int duration = mediaPlayer.getDuration();
        currentPosition = position;
        voiceTime.setText(VoiceTimeUtils.convertMilliSecondToMinute2(position)
                + "/" + VoiceTimeUtils.convertMilliSecondToMinute2(duration));
    }

    public void setMyPlayerCallback(MyPlayerCallback callback) {
        this.callback = callback;
    }

    public void play() {
        timing();
        mediaPlayer.start();
    }

    /**
     * 新开一个线程，播放音频。
     *
     * @param url media path
     */
    public void playUrl(final Context context,final String url) {
        if (mediaPlayer == null) {
            createMediaPlayer();
        }
        timing();
        service.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(context, Uri.parse(url));
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void pause() {
        handler.removeCallbacks(runnable);
        mediaPlayer.pause();
    }

    public void stop() {
        handler.removeCallbacks(runnable);
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * 通过onPrepared播放
     */
    @Override
    public void onPrepared(MediaPlayer arg0) {
        arg0.start();
        if (callback != null) {
            callback.onPrepared();
        }
    }

    @Override
    public void onCompletion(MediaPlayer arg0) {
        if (callback != null) {
            callback.onCompletion();
        }
        mediaPlayer.stop();
        mediaPlayer.reset();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer arg0, int bufferingProgress) {
    }
}
package com.ekuater.labelchat.ui.fragment.voice;

import java.io.File;
import java.util.Locale;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.httpfileloader.assist.FailReason;
import com.ekuater.httpfileloader.listener.FileLoadingListener;
import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.delegate.LabelStoryManager;
import com.ekuater.labelchat.ui.util.ShowToast;

public class PlayerMeidaVoice implements OnClickListener {

    private static final int STATE_IDLE = 0;
    private static final int STATE_LOADING = 1;
    private static final int STATE_PLAYING = 2;
    private int mState = -1;
    private LabelStoryManager mStoryManager;

    private Context context;
    private LabelStory story;
    private View mImageVoice;
    private View loadingAnim;
    private ImageView mPlayerVoice;
    private TextView duration;
    private Animation operatingAnim;
    public static PlayerMeidaVoice mPlayerMeidaVoice = null;
    public Player mPlayer;

    public PlayerMeidaVoice(Context context, LabelStory story, TextView duration, ImageView playerVoice,View mImageVoice, View loadingAnim) {

        this.context = context;
        this.mImageVoice = mImageVoice;
        this.story = story;
        this.loadingAnim = loadingAnim;
        this.duration = duration;
        mPlayerVoice = playerVoice;
        mPlayerMeidaVoice = this;
        mStoryManager = LabelStoryManager.getInstance(context);
        mPlayer = new Player(duration);
        mPlayer.setMyPlayerCallback(callback);
        operatingAnim = AnimationUtils.loadAnimation(context, R.anim.voice_player_anim);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        setState(STATE_IDLE);
    }

    public MyPlayerCallback callback = new MyPlayerCallback() {
        @Override
        public void onPrepared() {
            setState(STATE_PLAYING);
        }

        @Override
        public void onCompletion() {
            setState(STATE_IDLE);
        }
    };

    private FileLoadingListener fileLoadingListener = new FileLoadingListener() {
        @Override
        public void onLoadingStarted(String fileUri) {
            setState(STATE_LOADING);
        }

        @Override
        public void onLoadingFailed(String fileUri, FailReason failReason) {
            setState(STATE_IDLE);
            ShowToast.makeText(context, R.drawable.emoji_sad,
                    context.getString(R.string.voice_load_failed)).show();
        }

        @Override
        public void onLoadingComplete(String fileUri, File file) {
            if (file != null && file.exists()) {
                mPlayer.playUrl(context,file.getAbsolutePath());
                setState(STATE_LOADING);
            } else {
                setState(STATE_IDLE);
                ShowToast.makeText(context, R.drawable.emoji_sad,
                        context.getString(R.string.voice_load_failed)).show();
            }
        }

        @Override
        public void onLoadingCancelled(String fileUri) {
            setState(STATE_IDLE);
        }
    };

    private void updateUiState() {
        switch (mState) {
            case STATE_IDLE:
                mPlayerVoice.setEnabled(true);
                mPlayerVoice.setImageResource(R.drawable.ic_sound_play);
                duration.setText(getDuration(story.getDuration()));
                mImageVoice.clearAnimation();
                loadingAnim.setVisibility(View.GONE);
                break;
            case STATE_LOADING:
                mPlayerVoice.setEnabled(false);
                mPlayerVoice.setImageResource(R.drawable.ic_sound_stop);
                loadingAnim.setVisibility(View.VISIBLE);
                break;
            case STATE_PLAYING:
                mPlayerVoice.setEnabled(true);
                mPlayerVoice.setImageResource(R.drawable.ic_sound_stop);
                mImageVoice.startAnimation(operatingAnim);
                loadingAnim.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    private String getDuration(long duration) {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        return String.format(Locale.ENGLISH, "%1$02d:%2$02d", minutes % 60, seconds % 60);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        onPlayImageClick();
    }

    private void onPlayImageClick() {
        switch (mState) {
            case STATE_IDLE:
                loadAudioFile();
                break;
            case STATE_LOADING:
                break;
            case STATE_PLAYING:
                stopPlay();
                break;
            default:
                break;
        }
    }

    private void stopPlay() {
        mPlayer.stop();
        setState(STATE_IDLE);
    }

    private void setState(int state) {
        if (state != mState) {
            mState = state;
            updateUiState();
        }
    }

    private void loadAudioFile() {
        String fileUrl = story.getMedia();
        mStoryManager.loadAudioFile(fileUrl, fileLoadingListener);
    }
}


package com.ekuater.labelchat.ui.fragment.labelstory;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.httpfileloader.assist.FailReason;
import com.ekuater.httpfileloader.listener.FileLoadingListener;
import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.LabelStoryManager;
import com.ekuater.labelchat.ui.fragment.mixdynamic.audioplay.PlayListener;
import com.ekuater.labelchat.ui.fragment.mixdynamic.audioplay.Player;
import com.ekuater.labelchat.ui.fragment.voice.VoiceUtiles;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.ui.widget.CircleImageView;

import org.json.JSONObject;

import java.io.File;
import java.util.Locale;

/**
 * Created by Leo on 2015/4/20.
 *
 * @author LinYong
 */
public class AudioStoryRender implements StoryContentRender, View.OnClickListener,
        FileLoadingListener, PlayListener {

    private static final int STATE_IDLE = 0;
    private static final int STATE_LOADING = 1;
    private static final int STATE_PLAYING = 2;

    private Context mContext;
    private LabelStoryManager mStoryManager;
    private Player mPlayer;
    private int mState = -1;
    private Animation mPlayAnim;

    private TextView mTitleText;
    private ImageView mPlayImage;
    private TextView mDurationText;
    private CircleImageView mAnimView;
    private View mLoadView;
    private TextView mSongName;
    private TextView mSingerName;


    private LabelStory mBoundStory;

    public AudioStoryRender(Context context) {
        mContext = context;
        mStoryManager = LabelStoryManager.getInstance(context);
        mPlayAnim = AnimationUtils.loadAnimation(context, R.anim.voice_player_anim);
        mPlayAnim.setInterpolator(new LinearInterpolator());
    }

    @Override
    public void onCreate() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.audio_story_content, container, false);
        mTitleText = (TextView) view.findViewById(R.id.descript_content);
        mPlayImage = (ImageView) view.findViewById(R.id.play_image);
        mDurationText = (TextView) view.findViewById(R.id.duration);
        mAnimView = (CircleImageView) view.findViewById(R.id.play_anim);
        mLoadView = view.findViewById(R.id.loading);
        mSingerName = (TextView) view.findViewById(R.id.singer);
        mSongName = (TextView) view.findViewById(R.id.song);
        mPlayImage.setOnClickListener(this);
        mPlayer = new Player();
        mPlayer.setPlayListener(this);
        return view;
    }

    @Override
    public void bindContentData(LabelStory story) {
        mBoundStory = story;
        bindStory();
    }

    @Override
    public void onDestroyView() {
        releasePlay();
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_image:
                onPlayImageClick();
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoadingStarted(String fileUri) {
        setState(STATE_LOADING);
    }

    @Override
    public void onLoadingFailed(String fileUri, FailReason failReason) {
        setState(STATE_IDLE);
        ShowToast.makeText(mContext, R.drawable.emoji_sad,
                mContext.getString(R.string.voice_load_failed)).show();
    }

    @Override
    public void onLoadingComplete(String fileUri, File file) {
        if (file != null && file.exists()) {
            mPlayer.playUrl(file.getAbsolutePath());
            setState(STATE_LOADING);
        } else {
            setState(STATE_IDLE);
            ShowToast.makeText(mContext, R.drawable.emoji_sad,
                    mContext.getString(R.string.voice_load_failed)).show();
        }
    }

    @Override
    public void onLoadingCancelled(String fileUri) {
        setState(STATE_IDLE);
    }

    @Override
    public void onPrepared() {
        setState(STATE_PLAYING);
    }

    @Override
    public void onCompletion() {
        setState(STATE_IDLE);
    }

    @Override
    public void onTimeChanged(String time) {
        mDurationText.setText(mContext.getString(R.string.time, time));
    }

    private void bindStory() {
        if (mBoundStory.getImages() != null && mBoundStory.getImages().length > 0) {
            AvatarManager.getInstance(mContext).displaySingerAvatar(mBoundStory.getImages()[0], mAnimView, R.drawable.ic_sound_pic_normal);
        }else{
            mAnimView.setImageResource(R.drawable.sound_play_bg);
        }
        bingSong(mBoundStory.getType(), mBoundStory.getContent());
        setState(STATE_IDLE);
    }

    private void bingSong(String type, String content){
        if(LabelStory.TYPE_ONLINEAUDIO.equals(type)){
            JSONObject jsonObject = VoiceUtiles.getContentJson(content);
            if (jsonObject != null) {
                mSongName.setText(jsonObject.optString(CommandFields.Dynamic.SONG_NAME) == null ?
                        mContext.getString(R.string.song_name, mContext.getString(R.string.music_unknown)) :
                        mContext.getString(R.string.song_name, jsonObject.optString(CommandFields.Dynamic.SONG_NAME)));
                mSingerName.setText(jsonObject.optString(CommandFields.Dynamic.SINGER_NAME) == null ?
                        mContext.getString(R.string.singer_name, mContext.getString(R.string.music_unknown)) :
                        mContext.getString(R.string.singer_name, jsonObject.optString(CommandFields.Dynamic.SINGER_NAME)));
                mTitleText.setText(jsonObject.optString(CommandFields.Dynamic.DYNAMIC_CONTENT));
            }
        }else{
            mTitleText.setText(content);
            mSongName.setText(mContext.getString(R.string.song_name, mContext.getString(R.string.music_unknown)));
            mSingerName.setText(mContext.getString(R.string.singer_name, mContext.getString(R.string.music_unknown)));
        }
    }

    private String getDuration(long duration) {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        return String.format(Locale.ENGLISH, "%1$02d:%2$02d", minutes % 60, seconds % 60);
    }

    private void loadAudioFile() {
        String fileUrl = mBoundStory.getMedia();
        if (LabelStory.TYPE_ONLINEAUDIO.equals(mBoundStory.getType())) {
            mStoryManager.loadOnlineAudioFile(fileUrl, this);
        }else{
            mStoryManager.loadAudioFile(fileUrl, this);
        }
    }

    private void setState(int state) {
        if (state != mState) {
            mState = state;
            updateUiState();
        }
    }

    private void updateUiState() {
        switch (mState) {
            case STATE_IDLE:
                mPlayImage.setEnabled(true);
                mPlayImage.setBackgroundResource(R.drawable.record_play);
                mDurationText.setText(mContext.getString(R.string.time, getDuration(mBoundStory.getDuration())));
                mAnimView.clearAnimation();
                mLoadView.setVisibility(View.GONE);
                break;
            case STATE_LOADING:
                mPlayImage.setEnabled(false);
                mPlayImage.setBackgroundResource(R.drawable.record_stop);
                mLoadView.setVisibility(View.VISIBLE);
                break;
            case STATE_PLAYING:
                mPlayImage.setEnabled(true);
                mPlayImage.setBackgroundResource(R.drawable.record_stop);
                mAnimView.startAnimation(mPlayAnim);
                mLoadView.setVisibility(View.GONE);
                break;
            default:
                break;
        }
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

    private void releasePlay() {
        mPlayer.release();
        setState(STATE_IDLE);
    }
}

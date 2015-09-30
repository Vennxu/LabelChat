package com.ekuater.labelchat.ui.fragment.voice;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.Music;
import com.ekuater.labelchat.delegate.AlbumManager;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.LabelStoryManager;
import com.ekuater.labelchat.delegate.PostStoryListener;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.ui.fragment.ConfirmDialogFragment;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.ui.widget.CircleImageView;
import com.ekuater.labelchat.util.TextUtil;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by Administrator on 2015/4/15.
 */
public class VoicePushUI extends BackIconActivity implements View.OnClickListener, Handler.Callback {
    private static final int STATUS_PREPARE = 0;
    private static final int STATUS_RECORDING = 1;
    private static final int STATUS_PAUSE = 2;
    private static final int STATUS_PLAY_PREPARE = 3;
    private static final int STATUS_PLAY_PLAYING = 4;
    private static final int STATUS_PLAY_PAUSE = 5;
    private static final int RESPONSE_OUT_TIME = 100;
    private int status = STATUS_PREPARE;
    private CircleImageView voiceCd;
    private CircleImageView voiceTx;
    private EditText voiceText;
    private TextView voiceTime;
    private TextView rightTitle;
    private TextView musicName;
    private TextView songName;

    private Player player;
    private Animation operatingAnim;
    private AvatarManager avatarManager;
    private VoiceRecord record;
    private static final int MAX_LENGTH = 300 * 1000;
    private Handler handler = new Handler();
    private Runnable runnable;
    private LabelStoryManager labelStoryManager;
    private InputMethodManager mInputMethodManager;
    private Handler postHandler;
    private Music mMusic;
    private ImageView importView;
    private Button sendView;

    @Override
    public boolean handleMessage(Message msg) {
        boolean handle = true;
        switch (msg.what) {
            case VoiceUtiles.UPLOAD_VOICE_RESULT:
                if (this == null) {
                    break;
                }
                dismissProgressDialog();
                if (msg.arg1 == ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    ShowToast.makeText(this, R.drawable.emoji_smile,
                            this.getString(R.string.voice_upload_succese)).show();
                    this.finish();
                } else if (msg.arg1 == RESPONSE_OUT_TIME) {
                    ShowToast.makeText(this, R.drawable.emoji_smile,
                            this.getString(R.string.voice_upload_succese)).show();
                    this.finish();
                } else {
                    ShowToast.makeText(this, R.drawable.emoji_cry,
                            this.getString(R.string.voice_upload_defailt)).show();
                }
                rightTitle.setEnabled(false);
                rightTitle.setTextColor(getResources().getColor(R.color.colorLightDark));
                break;
            default:
                handle = false;
                break;
        }
        return handle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_push_voice);
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        record = new VoiceRecord();
        postHandler = new Handler(this);
        avatarManager = AvatarManager.getInstance(this);
        labelStoryManager = LabelStoryManager.getInstance(this);
        mInputMethodManager = (InputMethodManager) this.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.voice_player_anim);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        TextView title = (TextView) findViewById(R.id.title);
        ImageView icon = (ImageView) findViewById(R.id.icon);
        rightTitle = (TextView) findViewById(R.id.right_title);
        rightTitle.setText(getString(R.string.voice_player_restart));
        rightTitle.setTextColor(getResources().getColor(R.color.colorLightDark));
        rightTitle.setVisibility(View.VISIBLE);
        title.setText(getString(R.string.voice));
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        voiceCd = (CircleImageView) findViewById(R.id.push_voice_cd);
        voiceTx = (CircleImageView) findViewById(R.id.push_voice_tx);
        voiceText = (EditText) findViewById(R.id.push_voice_text);
        voiceText.addTextChangedListener(textWatcher);
        voiceTime = (TextView) findViewById(R.id.push_voice_time);
        importView = (ImageView) findViewById(R.id.import_music);
        musicName = (TextView) findViewById(R.id.push_voice_name);
        songName = (TextView) findViewById(R.id.push_voice_auther);
        sendView = (Button) findViewById(R.id.push_voice_send);
        rightTitle.setOnClickListener(this);
        importView.setOnClickListener(this);
        sendView.setOnClickListener(this);
        voiceTx.setOnClickListener(this);
        MiscUtils.showAvatarThumb(avatarManager, SettingHelper.getInstance(this).getAccountAvatarThumb(), voiceTx, R.drawable.contact_single);
//        voiceTime.setText(VoiceTimeUtils.convertMilliSecondToMinute2(0) + "/" + VoiceTimeUtils.convertMilliSecondToMinute2(MAX_LENGTH));
        player = new Player(voiceTime);
        player.setMyPlayerCallback(new MyPlayerCallback() {
            @Override
            public void onPrepared() {
            }

            @Override
            public void onCompletion() {
                voiceCd.clearAnimation();
                status = STATUS_PLAY_PREPARE;
                voiceTx.setImageResource(R.drawable.record_play);
                if (mMusic != null) {
                    voiceTime.setText(VoiceTimeUtils.convertMilliSecondToMinute2(0) + "/" + VoiceTimeUtils.convertMilliSecondToMinute2((int) mMusic.getDuration()));
                } else {
                    voiceTime.setText(VoiceTimeUtils.convertMilliSecondToMinute2(0) + "/" + VoiceTimeUtils.convertMilliSecondToMinute2(voiceLength));
                }
            }
        });
//        voiceText.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                voiceText.setCursorVisible(true);
//                voiceText.requestFocus();
//                mInputMethodManager.showSoftInput(v, InputMethodManager.SHOW_FORCED);
//                return true;
//            }
//        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            Music music = (Music) bundle.get(Music.class.getSimpleName());
            mMusic = music;
            if (mMusic != null) {
                rightTitle.setTextColor(getResources().getColor(R.color.white));
                voiceTx.setImageResource(R.drawable.record_play);
                voiceText.setText(getString(R.string.my_share, mMusic.getSongName()));
                voiceText.setSelection(getString(R.string.my_share, mMusic.getSongName()).length());
                voiceTime.setText(VoiceTimeUtils.convertMilliSecondToMinute2(0) +
                        "/" + VoiceTimeUtils.convertMilliSecondToMinute2((int) mMusic.getDuration()));
                musicName.setText(getString(R.string.singer_name, mMusic.getSingerName()));
                songName.setText(getString(R.string.song_name, mMusic.getSongName()));
                avatarManager.displaySingerAvatar(mMusic.getSingerPic(), voiceCd, R.drawable.ic_sound_pic_normal);
                player.stop();
                voiceCd.clearAnimation();
                sendView.setEnabled(true);
                status = STATUS_PLAY_PREPARE;
            }
        }
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() > 0) {
                if (mMusic != null) {
                    sendView.setEnabled(true);
                }
            } else {
                sendView.setEnabled(false);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.push_voice_tx:
                if (mMusic != null) {
                    handleRecord();
                }
                break;
            case R.id.right_title:
                status = STATUS_PLAY_PLAYING;
                voiceCd.clearAnimation();
                voiceCd.startAnimation(operatingAnim);
                if (mMusic != null) {
                    player.playUrl(VoicePushUI.this, mMusic.getMusicUrl());
                } else {
                    if (record.getPlayRecord() != null && record.getPlayRecord().exists()) {
                        player.playUrl(VoicePushUI.this, record.getPlayRecord().getPath());
                    }
                }
                voiceTx.setImageResource(R.drawable.record_pause);
                break;
            case R.id.import_music:
                UILauncher.launchMusicListUI(VoicePushUI.this, 200);
                break;
            case R.id.push_voice_send:
                String content = voiceText.getText().toString();
                if (mMusic == null) {
                    if (TextUtil.isEmpty(content) && (record.getPlayRecord() == null || !record.getPlayRecord().exists())) {
                        return;
                    }
                    if (TextUtil.isEmpty(content)) {
                        ShowToast.makeText(this, R.drawable.emoji_smile, getString(R.string.voice_upload_content)).show();
                    } else if (record.getPlayRecord() == null || !record.getPlayRecord().exists()) {
                        ShowToast.makeText(this, R.drawable.emoji_smile, getString(R.string.voice_upload_file)).show();
                    } else if (!TextUtil.isEmpty(content) && (record.getPlayRecord() != null && record.getPlayRecord().exists())) {
                        try {
                            uploadVoice(content, record.getPlayRecord(), voiceLength);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        uploadVoice(content, new File(mMusic.getMusicUrl()), mMusic.getDuration());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;

        }
    }


    public void handleRecord() {
        switch (status) {
            case STATUS_PLAY_PREPARE:
                status = STATUS_PLAY_PLAYING;
                if (mMusic != null) {
                    player.playUrl(VoicePushUI.this, mMusic.getMusicUrl());
                } else {
                    if (record.getPlayRecord() != null && record.getPlayRecord().exists()) {
                        player.playUrl(VoicePushUI.this, record.getPlayRecord().getPath());
                    }
                }
                voiceCd.startAnimation(operatingAnim);
                voiceTx.setImageResource(R.drawable.record_pause);
                break;
            case STATUS_PLAY_PLAYING:
                status = STATUS_PLAY_PAUSE;
                player.pause();
                voiceCd.clearAnimation();
                voiceTx.setImageResource(R.drawable.record_play);
                break;
            case STATUS_PLAY_PAUSE:
                status = STATUS_PLAY_PLAYING;
                player.play();
                voiceCd.startAnimation(operatingAnim);
                voiceTx.setImageResource(R.drawable.record_pause);
                break;
        }
    }

    private Drawable setDrawabelTop(int drawabel) {
        Drawable rightDrawable = getResources().getDrawable(drawabel);
        rightDrawable.setBounds(0, 0, rightDrawable.getMinimumWidth(), rightDrawable.getMinimumHeight());
        return rightDrawable;
    }


    public void pauseAudioRecord() {
        record.puaseRecord();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
            runnable = null;
        }
    }

    public void stopAudioRecord() {
        pauseAudioRecord();
        status = STATUS_PLAY_PREPARE;
    }

    public void resetAudioRecord() {
        if (status == STATUS_PLAY_PAUSE || status == STATUS_PLAY_PLAYING) {
            player.stop();
            voiceTx.clearAnimation();
        }
        if (status == STATUS_PAUSE || status == STATUS_RECORDING) {
            record.restartRecord();
        }
        status = STATUS_PREPARE;
    }

    private ConfirmDialogFragment.AbsConfirmListener confirmListener = new ConfirmDialogFragment.AbsConfirmListener() {
        @Override
        public void onConfirm() {
            voiceTime.setText(VoiceTimeUtils.convertMilliSecondToMinute2(0) + "/" + VoiceTimeUtils.convertMilliSecondToMinute2(MAX_LENGTH));
            voiceText.setText("");
            voiceText.setHint(getResources().getString(R.string.voice_push_hint));
            resetAudioRecord();
            mMusic = null;
//            record.deleteAllFiles(1);
        }

        @Override
        public void onCancel() {
            super.onCancel();
        }

    };

    private int voiceLength;

    private void timing() {
        runnable = new Runnable() {
            @Override
            public void run() {
                voiceLength += 1000;
                if (voiceLength > MAX_LENGTH) {
//                    stopAudioRecord();
                } else {
                    voiceTime.setText(VoiceTimeUtils.convertMilliSecondToMinute2(voiceLength) + "/" + VoiceTimeUtils.convertMilliSecondToMinute2(MAX_LENGTH));
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    private void uploadVoice(String title, File file, long duration) throws FileNotFoundException {
        showProgressDialog();
        rightTitle.setEnabled(false);
        rightTitle.setTextColor(getResources().getColor(R.color.colorLightDark));
        if (mMusic != null){
            title = VoiceUtiles.setContentJson(mMusic, title);
        }
        labelStoryManager.postOnlineAudioStory(AlbumManager.getInstance(this).getRelatedUser(), title, mMusic.getSingerPic(), file, mMusic.getMusicUrl(), duration, new PostStoryListener() {
            @Override
            public void onPostResult(int result, int errorCode, String errorDesc, LabelStory[] labelStories) {
                postHandler.obtainMessage(VoiceUtiles.UPLOAD_VOICE_RESULT, result, 0, labelStories).sendToTarget();
            }
        });
    }

    private SimpleProgressDialog mProgressDialog;

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = SimpleProgressDialog.newInstance();
            mProgressDialog.show(getSupportFragmentManager(), "SimpleProgressDialog");
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void showConfirmDialog() {
        ConfirmDialogFragment.UiConfig uiConfig = new ConfirmDialogFragment.UiConfig(this.getString(R.string.voice_confirm_restart), null);
        ConfirmDialogFragment confirmDialogFragment = ConfirmDialogFragment.newInstance(uiConfig, confirmListener);
        confirmDialogFragment.show(getSupportFragmentManager(), "ConfideShowFragment");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.stop();
        record.restartRecord();
        record.deletRecordFile();
    }
}

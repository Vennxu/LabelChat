package com.ekuater.labelchat.ui.activity.chatting;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.ekuater.labelchat.EnvConfig;
import com.ekuater.labelchat.util.L;

import java.io.File;
import java.io.FileInputStream;

/**
 * @author LinYong
 */
/*package*/ final class RecordPlayer implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener {

    public interface IPlayListener {

        public void onPlayStarted();

        public void onPlayCompleted();

        public void onPlayStopped();

        public void onPlayError();
    }

    private class ProximitySensorListener implements ProximitySensorManager.Listener {

        @Override
        public synchronized void onNear() {
            onPSensorNear();
        }

        @Override
        public synchronized void onFar() {
            onPSensorFar();
        }
    }

    private static final String TAG = RecordPlayer.class.getSimpleName();

    private final ProximitySensorManager mProximitySensorManager;
    private final AudioManager mAudioManager;

    private File mRecordDir;
    private MediaPlayer mPlayer;
    private boolean mPlaying = false;
    private boolean mIsNear = false;

    private IPlayListener mCurrPlayListener;
    private String mCurrPlayFile;
    private int mCurrStreamType = -1;
    private int mCurrStreamVolume = -1;

    public RecordPlayer(Context context, String userId) {
        mProximitySensorManager = new ProximitySensorManager(context,
                new ProximitySensorListener());
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mRecordDir = EnvConfig.getVoiceChatMsgDirectory(userId);
        if ((!mRecordDir.exists() && !mRecordDir.mkdirs())
                || (mRecordDir.exists() && mRecordDir.isFile())) {
            L.v(TAG, "make record dir failed, dir=" + mRecordDir);
        }
    }

    public synchronized void play(String fileName, IPlayListener listener) {
        L.v(TAG, "play()" + ", file=" + fileName);

        if (mCurrPlayFile != null && mCurrPlayFile.equals(fileName)) {
            // the file now playing, just return;
            return;
        }

        if (mPlaying) {
            stopPlaying();
            notifyPlayStopped();
            mCurrPlayListener = null;
            mCurrPlayFile = null;
        }

        mCurrPlayFile = fileName;
        mCurrPlayListener = listener;
        mPlaying = startPlaying();

        if (mPlaying) {
            notifyPlayStarted();
            mProximitySensorManager.enable();
        } else {
            notifyPlayError();
            onPlayCompleted();
        }
    }

    public synchronized void stop() {
        if (mPlaying) {
            stopPlaying();
            onPlayCompleted();
        }
    }

    private File getRecordFile(String fileName) {
        return new File(mRecordDir, fileName);
    }

    private synchronized void onPlayCompleted() {
        mProximitySensorManager.disable(false);
        mPlaying = false;
        mCurrPlayFile = null;
        mCurrPlayListener = null;
        mIsNear = false;
        mCurrStreamType = -1;
        mCurrStreamVolume = -1;
        mPlayer = null;
    }

    private boolean startPlaying() {
        boolean _ret = false;

        try {
            mCurrStreamType = mIsNear ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC;

            mPlayer = new MediaPlayer();
            mPlayer.reset();
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
            mPlayer.setAudioStreamType(mCurrStreamType);
            mPlayer.setOnPreparedListener(this);
            mPlayer.setDataSource(new FileInputStream(getRecordFile(mCurrPlayFile)).getFD());
            mPlayer.prepareAsync();

            mCurrStreamVolume = mAudioManager.getStreamVolume(mCurrStreamType);
            mAudioManager.setStreamVolume(mCurrStreamType,
                    mAudioManager.getStreamMaxVolume(mCurrStreamType), 0);

            _ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return _ret;
    }

    private void stopPlaying() {
        mAudioManager.setStreamVolume(mCurrStreamType, mCurrStreamVolume, 0);

        mPlayer.stop();
        mPlayer.setOnCompletionListener(null);
        mPlayer.setOnErrorListener(null);
        mPlayer.release();
    }

    private synchronized void replay() {
        if (mPlaying) {
            stopPlaying();
            startPlaying();
        }
    }

    private void onPSensorNear() {
        mIsNear = true;
        replay();
    }

    private void onPSensorFar() {
        mIsNear = false;
        replay();
    }

    private void notifyPlayStarted() {
        if (mCurrPlayListener != null) {
            mCurrPlayListener.onPlayStarted();
        }
    }

    private void notifyPlayStopped() {
        if (mCurrPlayListener != null) {
            mCurrPlayListener.onPlayStopped();
        }
    }

    private void notifyPlayCompleted() {
        if (mCurrPlayListener != null) {
            mCurrPlayListener.onPlayCompleted();
        }
    }

    private void notifyPlayError() {
        if (mCurrPlayListener != null) {
            mCurrPlayListener.onPlayError();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mPlayer == mp) {
            mPlayer.start();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        notifyPlayCompleted();
        onPlayCompleted();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        notifyPlayError();
        onPlayCompleted();
        return false;
    }
}

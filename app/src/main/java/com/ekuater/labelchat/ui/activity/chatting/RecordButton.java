package com.ekuater.labelchat.ui.activity.chatting;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.ekuater.labelchat.EnvConfig;
import com.ekuater.labelchat.R;
import com.ekuater.labelchat.util.UniqueFileName;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author LinYong
 */
public class RecordButton extends PressButton {

    public static final int RECORD_RESULT_SUCCESS = 0;
    public static final int RECORD_RESULT_START_RECORD_FAILED = 1;
    public static final int RECORD_RESULT_RECORD_TIME_TOO_SHORT = 2;
    public static final int RECORD_RESULT_MANUAL_CANCELED = 3;

    public interface IRecordListener {
        public void onStart();

        public void onCanceled(int result);

        public void onFinished(String recordFileName, long recordTime);

        public void onFailure(int result);
    }

    private static final String TAG = RecordButton.class.getSimpleName();
    private static final int MIN_INTERVAL_TIME = 1000;// 1s

    private static final class RecordTask extends AsyncTask<Void, Integer, Integer> {

        private static final int STOP_FLAG_STOP = 1;
        private static final int STOP_FLAG_CANCEL = 2;

        private final RecordButton mRecordButton;
        private final AtomicInteger mStopFlag = new AtomicInteger();
        private final File mRecordFile;
        private long mRecordTime = -1;

        public RecordTask(RecordButton recordButton, File recordFile) {
            super();
            mRecordButton = recordButton;
            mRecordFile = recordFile;
        }

        public File getRecordFile() {
            return mRecordFile;
        }

        public long getRecordTime() {
            return mRecordTime;
        }

        private boolean isContinue() {
            return mStopFlag.get() == 0;
        }

        public void stop() {
            mStopFlag.set(STOP_FLAG_STOP);
        }

        public void cancel() {
            mStopFlag.set(STOP_FLAG_CANCEL);
        }

        private void deleteRecordFile() {
            if (!mRecordFile.delete()) {
                Log.w(TAG, "Record time too short, delete record file failed, file="
                        + mRecordFile);
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            long recordStartTime = System.currentTimeMillis();
            MediaRecorder mediaRecorder = null;

            // start recording
            try {
                mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setAudioChannels(1);
                mediaRecorder.setAudioEncodingBitRate(4000);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mediaRecorder.setOutputFile(mRecordFile.getPath());
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (Exception e) {
                e.printStackTrace();

                if (mediaRecorder != null) {
                    mediaRecorder.reset();
                    mediaRecorder.release();
                }

                return RECORD_RESULT_START_RECORD_FAILED;
            }

            // update amplitude
            while (isContinue()) {
                int amplitude = mediaRecorder.getMaxAmplitude();
                int level = (amplitude > 0) ? (int) (10 * Math.log(amplitude) / Math.log(10)) : 1;
                publishProgress(Math.min(100, level));
                // Log.d(TAG, "amplitude=" + amplitude + ",level=" + level);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // finish recording
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            }

            long totalTime = System.currentTimeMillis() - recordStartTime;

            if (mStopFlag.get() == STOP_FLAG_CANCEL) {
                deleteRecordFile();
                return RECORD_RESULT_MANUAL_CANCELED;
            } else if (totalTime < MIN_INTERVAL_TIME) {
                deleteRecordFile();
                return RECORD_RESULT_RECORD_TIME_TOO_SHORT;
            } else {
                mRecordTime = totalTime;
                return RECORD_RESULT_SUCCESS;
            }
        }

        @Override
        protected void onPreExecute() {
            mRecordButton.initRecordUI();
            mRecordButton.showRecordUI();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            mRecordButton.dismissRecordUI();
            mRecordButton.deInitRecordUI();
            mRecordButton.onRecordingFinished(integer);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mRecordButton.updateRecordAmplitude(values[0]);
        }
    }

    private final IPressListener mPressListener = new IPressListener() {
        @Override
        public void onPressChanged(boolean pressed, boolean cancel) {
            if (pressed) {
                startRecording();
            } else if (cancel) {
                cancelRecording();
            } else {
                finishRecording();
            }

            setText(pressed ? R.string.release_to_send : R.string.press_to_record);
        }
    };

    private IRecordListener mRecordListener;
    private File mRecordDir;
    private RecordTask mRecordTask;

    private Dialog mIndicatorDialog;
    private ImageView mIndicatorView;

    public RecordButton(Context context) {
        super(context);
        init();
    }

    public RecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RecordButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setPressListener(mPressListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setPressListener(null);
    }

    public void setRecordListener(IRecordListener l) {
        mRecordListener = l;
    }

    public void setUserId(String userId) {
        mRecordDir = EnvConfig.getVoiceChatMsgDirectory(userId);
    }

    private File getRecordFile() {
        return new File(mRecordDir, UniqueFileName.getUniqueFileName("amr"));
    }

    private void startRecording() {
        if (mRecordTask == null) {
            if (mRecordListener != null) {
                mRecordListener.onStart();
            }
            mRecordTask = new RecordTask(this, getRecordFile());
            mRecordTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void cancelRecording() {
        if (mRecordTask != null) {
            mRecordTask.cancel();
        }
    }

    private void finishRecording() {
        if (mRecordTask != null) {
            mRecordTask.stop();
        }
    }

    private void updateRecordAmplitude(int level) {
        if (mIndicatorView != null) {
            mIndicatorView.setImageLevel(level);
        }
    }

    private void onRecordingFinished(int result) {
        if (mRecordTask != null) {
            File recordFile = mRecordTask.getRecordFile();
            long recordTime = mRecordTask.getRecordTime();
            mRecordTask = null;

            switch (result) {
                case RECORD_RESULT_SUCCESS:
                    if (mRecordListener != null) {
                        mRecordListener.onFinished(recordFile.getName(), recordTime);
                    }
                    break;
                case RECORD_RESULT_RECORD_TIME_TOO_SHORT:
                case RECORD_RESULT_MANUAL_CANCELED:
                    if (mRecordListener != null) {
                        mRecordListener.onCanceled(result);
                    }
                    break;
                case RECORD_RESULT_START_RECORD_FAILED:
                default:
                    if (mRecordListener != null) {
                        mRecordListener.onFailure(result);
                    }
                    break;
            }
        }
    }

    private void initRecordUI() {
        Context context = getContext();
        mIndicatorDialog = new Dialog(context, R.style.ChatUIRecordIndicatorDialog);
        View dialogView = LayoutInflater.from(context).inflate(
                R.layout.sound_record_indicator, null);
        mIndicatorView = (ImageView) dialogView.findViewById(R.id.indicator);
        mIndicatorView.setImageResource(R.drawable.chatting_ui_record_amplitude_indicator);
        mIndicatorDialog.setContentView(dialogView);
        WindowManager.LayoutParams lp = mIndicatorDialog.getWindow().getAttributes();
        lp.gravity = Gravity.CENTER;
    }

    private void showRecordUI() {
        mIndicatorDialog.show();
    }

    private void dismissRecordUI() {
        mIndicatorDialog.dismiss();
    }

    private void deInitRecordUI() {
        mIndicatorView = null;
        mIndicatorDialog = null;
    }
}

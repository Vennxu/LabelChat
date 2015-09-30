package com.ekuater.labelchat.ui.fragment.voice;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class VoiceRecord {
	private MediaRecorder recorder;
    private File recAudioFile;
    private File recAudioDir;
    private ArrayList<String> list;
    private final String SUFFIX=".amr";
    private String pathStr = Environment.getExternalStorageDirectory().getPath()+"/YYT";
    private VoiceRecordManager manager;
    private File playRecordFile;
    public VoiceRecord(){
        manager = new VoiceRecordManager();
        list = new ArrayList<>();
        recAudioDir= new File(pathStr);
        if(!recAudioDir.exists()){
            recAudioDir.mkdirs();
            Log.d("record", "创建录音文件！" + recAudioDir.exists());
        }
    }

	public void startRecord() {
        String mMinute1 = manager.getTime();
        recAudioFile=new File(recAudioDir, mMinute1+SUFFIX);
        Log.d("record", recAudioFile.getPath());
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(recAudioFile.getPath());
		try {
			recorder.prepare();
			recorder.start();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		recorder.setOnInfoListener(new OnInfoListener() {
			@Override
			public void onInfo(MediaRecorder mr, int what, int extra) {
				// TODO Auto-generated method stub
				int a = mr.getMaxAmplitude();
			}
		});
	}

    public void puaseRecord(){
        list.add(recAudioFile.getPath());
        stopRecord();
    }

    public void finishRecord(){
        if(list.size() == 1){
            playRecordFile = recAudioFile;
        }else {
            playRecordFile = manager.getInputCollection(list, recAudioDir);
        }
        list.clear();
    }

    public File getPlayRecord(){
        return playRecordFile != null && playRecordFile.exists() ? playRecordFile:null;
    }

    public void restartRecord(){
        stopRecord();
        manager.deleteListRecord(list);
        list.clear();
    }

    public void deletRecordFile(){
        manager.deleteListRecord(list);
    }

	public void stopRecord() {
		if (recorder != null) {
			recorder.stop();
			recorder.release();
			recorder = null;
		}
	}

}

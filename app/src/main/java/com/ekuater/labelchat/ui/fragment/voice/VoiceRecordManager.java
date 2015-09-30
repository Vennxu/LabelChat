package com.ekuater.labelchat.ui.fragment.voice;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.util.Log;

public class VoiceRecordManager {

    private static final String SUFFIX = ".amr";

    public File getInputCollection(List list, File recAudioDir) {
        String mMinute1 = getTime();
        // 创建音频文件,合并的文件放这里
        File file1 = new File(recAudioDir, mMinute1 + SUFFIX);
        Log.d("record", file1.getPath()+ "");
        FileOutputStream fileOutputStream = null;
        if (!file1.exists()) {
            try {
                file1.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            fileOutputStream = new FileOutputStream(file1);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // list里面为暂停录音 所产生的 几段录音文件的名字，中间几段文件的减去前面的6个字节头文件
        for (int i = 0; i < list.size(); i++) {
            File file = new File((String) list.get(i));
            Log.d("list的长度", file.getPath()+ "");
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] myByte = new byte[fileInputStream.available()];
                // 文件长度
                int length = myByte.length;
                // 头文件
                if (i == 0) {
                    while (fileInputStream.read(myByte) != -1) {
                        fileOutputStream.write(myByte, 0, length);
                    }
                }
                // 之后的文件，去掉头文件就可以了
                else {
                    while (fileInputStream.read(myByte) != -1) {
                        fileOutputStream.write(myByte, 6, length - 6);
                    }
                }
                fileOutputStream.flush();
                fileInputStream.close();
                System.out.println("合成文件长度：" + file1.length());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 结束后关闭流
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        deleteListRecord(list);
        return file1;
    }

    public  void deleteListRecord(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            File file = new File((String) list.get(i));
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public static String getTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日HH：mm：ss");
        Date curDate = new Date(System.currentTimeMillis());
        String time = formatter.format(curDate);
        System.out.println("当前时间");
        return time;
    }
}

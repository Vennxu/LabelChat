package com.ekuater.labelchat.ui.util;


import android.content.Context;

import org.apache.http.util.EncodingUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by wenxiang on 2015/3/3.
 */
public class FileUtils {

    public static void writeFileData(String fileName,String message,Context context){
        try{
            FileOutputStream fout =context.openFileOutput(fileName, context.MODE_PRIVATE);
            byte [] bytes = message.getBytes();
            fout.write(bytes);
            fout.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public static String readFileData(String fileName,Context context){
        String res="";
        try{
            FileInputStream fin =context.openFileInput(fileName);
            int length = fin.available();
            byte [] buffer = new byte[length];
            fin.read(buffer);
            res = new String(buffer);
//            res = EncodingUtils.getString(buffer, "GBK");
            fin.close();
        }catch(Exception e){
            return null;
        }
        return res;
    }

    public static void deletFileData(String fileName){
        File file = new File(fileName);
        try{
            file.delete();
        }catch(Exception e){

        }

    }
}

package com.ekuater.labelchat.delegate;

import android.content.Context;

import com.ekuater.httpfileloader.FileLoader;
import com.ekuater.httpfileloader.FileLoaderConfiguration;
import com.ekuater.httpfileloader.assist.QueueProcessingType;
import com.ekuater.httpfileloader.disc.naming.Md5FileNameGenerator;

/**
 * Created by Leo on 2015/4/15.
 *
 * @author LinYong
 */
public class FileLoaderHelper {

    private static final int DISK_CACHE_SIZE = 100 * 1024 * 1024; // 200 MB

    private static FileLoader mFileLoader;

    private static synchronized void initFileLoader(Context context) {
        if (mFileLoader == null) {
            mFileLoader = FileLoader.getInstance();
            FileLoaderConfiguration.Builder builder
                    = new FileLoaderConfiguration.Builder(context.getApplicationContext())
                    .threadPoolSize(3)
                    .threadPriority(Thread.NORM_PRIORITY - 2)
                    .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                    .diskCacheSize(DISK_CACHE_SIZE)
                    .tasksProcessingOrder(QueueProcessingType.FIFO);
            mFileLoader.init(builder.build());
        }
    }

    public static FileLoader getFileLoader(Context context) {
        if (mFileLoader == null) {
            initFileLoader(context);
        }
        return mFileLoader;
    }
}
